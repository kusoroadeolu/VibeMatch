package com.victor.VibeMatch.connections;

public record ConnectionWrapperResponseDto(
        ActiveConnectionResponseDto activeConnectionResponseDto,
        InactiveConnectionResponseDto inactiveConnectionResponseDto
) {
}
