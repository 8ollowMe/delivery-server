package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;

public class ForbiddenException extends BusinessException {
	public ForbiddenException() {
		super(CommonErrorCode.FORBIDDEN);
	}
}
