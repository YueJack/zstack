package org.zstack.core.captcha;

import org.zstack.header.vo.ResourceVO_;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.sql.Timestamp;

/**
 * Created by kayo on 2018/7/5.
 */
@StaticMetamodel(CaptchaVO.class)
public class CaptchaVO_ extends ResourceVO_ {
    public static volatile SingularAttribute<CaptchaVO, String> captcha;
    public static volatile SingularAttribute<CaptchaVO, String> verifyCode;
    public static volatile SingularAttribute<CaptchaVO, String> targetResourceUuid;
    public static volatile SingularAttribute<CaptchaVO, Integer> attempts;
    public static volatile SingularAttribute<CaptchaVO, Timestamp> createDate;
    public static volatile SingularAttribute<CaptchaVO, Timestamp> lastOpDate;
}
