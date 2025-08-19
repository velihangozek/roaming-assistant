package com.turkcell.roaming.roaming_assistant.business.abstracts;

import com.turkcell.roaming.roaming_assistant.dto.requests.Selection;

public interface CheckoutService {
    String checkout(Long userId, Selection selection);
}