package com.umc.edison.data.token

class NoRefreshTokenException : IllegalStateException("No refresh token")
class RefreshFailedException(message: String) : IllegalStateException(message)
