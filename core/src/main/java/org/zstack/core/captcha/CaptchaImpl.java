package org.zstack.core.captcha;

import org.apache.commons.codec.binary.Base64;
import org.patchca.color.ColorFactory;
import org.patchca.filter.predefined.*;
import org.patchca.service.ConfigurableCaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.db.Q;
import org.zstack.core.db.SQL;
import org.zstack.header.AbstractService;
import org.zstack.header.Component;
import org.zstack.header.core.Completion;
import org.zstack.header.core.ReturnValueCompletion;
import org.zstack.header.errorcode.ErrorCode;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.Message;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import static org.zstack.core.Platform.operr;


/**
 * Created by kayo on 2018/7/4.
 */
public class CaptchaImpl extends AbstractService implements Component, Captcha {
    private static final CLogger logger = Utils.getLogger(CaptchaImpl.class);

    @Autowired
    private DatabaseFacade dbf;
    @Autowired
    private CloudBus bus;

    private String CAPTCHA_FILE_TYPE = "png";

    private static ConfigurableCaptchaService cs;

    static {
        cs = new ConfigurableCaptchaService();

        Random random = new Random();

        cs.setColorFactory(new ColorFactory() {
            @Override
            public Color getColor(int x) {
                int[] c = new int[3];
                int i = random.nextInt(c.length);
                for (int fi = 0; fi < c.length; fi++) {
                    if (fi == i) {
                        c[fi] = random.nextInt(71);
                    } else {
                        c[fi] = random.nextInt(256);
                    }
                }
                return new Color(c[0], c[1], c[2]);
            }
        });

        switch (random.nextInt(5)) {
            case 0:
                cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));
                break;
            case 1:
                cs.setFilterFactory(new MarbleRippleFilterFactory());
                break;
            case 2:
                cs.setFilterFactory(new DoubleRippleFilterFactory());
                break;
            case 3:
                cs.setFilterFactory(new WobbleRippleFilterFactory());
                break;
            case 4:
                cs.setFilterFactory(new DiffuseRippleFilterFactory());
                break;
        }
    }

    @Override
    public int getAttemptsForCurrentResource(String resourceUuid) {
        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.targetResourceUuid, resourceUuid).find();

        return vo.getAttempts();
    }

    @Override
    public void increaseAttemptCount(String resourceUuid) {
        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.targetResourceUuid, resourceUuid).find();
        vo.setAttempts(vo.getAttempts() + 1);
        dbf.update(vo);
    }

    @Override
    public void resetAttemptCount(String resourceUuid) {
        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.targetResourceUuid, resourceUuid).find();
        vo.setAttempts(0);
        vo.setVerifyCode("");
        vo.setCaptcha("");
        dbf.update(vo);
    }

    private String bytesToBase64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);// 返回Base64编码过的字节数组字符串
    }

    @Override
    public void generateCaptcha(String targetResourceUuid, ReturnValueCompletion<CaptchaStruct> completion) {
        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.targetResourceUuid, targetResourceUuid).find();

        if (!vo.getVerifyCode().equals("")) {
            CaptchaStruct struct = new CaptchaStruct();
            struct.setUuid(vo.getUuid());
            struct.setCaptcha(vo.getCaptcha());

            completion.success(struct);
            return;
        }

        String fileName = vo.getUuid();

        String verifyCode = "";
        String base64Image = "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            org.patchca.service.Captcha captcha = cs.getCaptcha();
            ImageIO.write(captcha.getImage(), CAPTCHA_FILE_TYPE, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            base64Image = bytesToBase64(imageInByte);
            verifyCode = captcha.getChallenge();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SQL.New(CaptchaVO.class).eq(CaptchaVO_.uuid, fileName)
                .set(CaptchaVO_.captcha, base64Image)
                .set(CaptchaVO_.verifyCode, verifyCode)
                .update();

        CaptchaStruct struct = new CaptchaStruct();
        struct.setUuid(fileName);
        struct.setCaptcha(base64Image);

        completion.success(struct);
    }

    public CaptchaVO refreshCaptcha(String uuid) {
        String verifyCode = "";
        String base64Image = "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            org.patchca.service.Captcha captcha = cs.getCaptcha();
            ImageIO.write(captcha.getImage(), CAPTCHA_FILE_TYPE, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            base64Image = bytesToBase64(imageInByte);
            verifyCode = captcha.getChallenge();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.uuid, uuid).find();
        vo.setCaptcha(base64Image);
        vo.setVerifyCode(verifyCode);
        return dbf.updateAndRefresh(vo);
    }

    @Override
    public void verifyCaptcha(String uuid, String verifyCode, Completion completion) {
        if (verifyCaptcha(uuid, verifyCode)) {
            completion.success();
            return;
        }

        refreshCaptcha(uuid);
        completion.fail(operr("Verify code not match"));
    }

    @Override
    public void refreshCaptcha(String uuid, ReturnValueCompletion<CaptchaStruct> completion) {
        CaptchaVO vo = refreshCaptcha(uuid);

        CaptchaStruct struct = new CaptchaStruct();
        struct.setCaptcha(vo.getCaptcha());
        struct.setUuid(vo.getUuid());

        completion.success(struct);
    }

    @Override
    public boolean verifyCaptcha(String uuid, String verifyCode) {
        CaptchaVO vo = Q.New(CaptchaVO.class).eq(CaptchaVO_.uuid, uuid).eq(CaptchaVO_.verifyCode, verifyCode).find();
        if(vo != null) {
            return true;
        }

        refreshCaptcha(uuid);
        return false;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg instanceof APIMessage) {
            handleApiMessage((APIMessage) msg);
        }
    }

    private void handleApiMessage(APIMessage msg) {
        if (msg instanceof APIRefreshCaptchaMsg) {
            handle((APIRefreshCaptchaMsg) msg);
        }
    }

    private void handle(APIRefreshCaptchaMsg msg) {
        APIRefreshCaptchaReply reply = new APIRefreshCaptchaReply();
        CaptchaVO vo = dbf.findByUuid(msg.getUuid(), CaptchaVO.class);

        refreshCaptcha(vo.getUuid(), new ReturnValueCompletion<CaptchaStruct>(msg) {
            @Override
            public void success(CaptchaStruct struct) {
                reply.setCaptcha(struct.getCaptcha());
                reply.setCaptchaUuid(struct.getUuid());

                bus.reply(msg, reply);
            }

            @Override
            public void fail(ErrorCode errorCode) {
                reply.setError(errorCode);

                bus.reply(msg, reply);
            }
        });
    }

    @Override
    public String getId() {
        return bus.makeLocalServiceId(CaptchaConstant.SERVICE_ID);
    }
}
