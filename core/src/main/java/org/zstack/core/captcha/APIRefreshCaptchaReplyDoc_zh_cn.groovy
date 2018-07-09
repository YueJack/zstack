package org.zstack.core.captcha

import org.zstack.header.errorcode.ErrorCode
import org.zstack.header.errorcode.ErrorCode

doc {

	title "在这里输入结构的名称"

	ref {
		name "error"
		path "org.zstack.core.captcha.APIRefreshCaptchaReply.error"
		desc "错误码，若不为null，则表示操作失败, 操作成功时该字段为null",false
		type "ErrorCode"
		since "0.6"
		clz ErrorCode.class
	}
	field {
		name "captchaUuid"
		desc ""
		type "String"
		since "0.6"
	}
	field {
		name "captcha"
		desc ""
		type "String"
		since "0.6"
	}
	field {
		name "success"
		desc ""
		type "boolean"
		since "0.6"
	}
	ref {
		name "error"
		path "org.zstack.core.captcha.APIRefreshCaptchaReply.error"
		desc "null"
		type "ErrorCode"
		since "0.6"
		clz ErrorCode.class
	}
}
