package com.turkcell.roaming.roaming_assistant.business.concretes;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CheckoutService;
import com.turkcell.roaming.roaming_assistant.dto.requests.Selection;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CheckoutManager implements CheckoutService {

    @Override
    public String checkout(Long userId, Selection selection) {
        // Basit doğrulama/örnek kullanım
        if ("pack".equalsIgnoreCase(selection.kind())) {
            if (selection.packId() == null || selection.nPacks() == null || selection.nPacks() <= 0) {
                throw new IllegalArgumentException("pack selection requires pack_id and positive n_packs");
            }
            // burada packId ve nPacks ile siparişi kaydettiğini varsay
        } else if (!"payg".equalsIgnoreCase(selection.kind())) {
            throw new IllegalArgumentException("selection.kind must be 'pack' or 'payg'");
        }

        // Mock sipariş numarası
        return "MOCK-" + UUID.randomUUID();
    }
}
