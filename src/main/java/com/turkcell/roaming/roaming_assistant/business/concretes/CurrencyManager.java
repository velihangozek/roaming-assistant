package com.turkcell.roaming.roaming_assistant.business.concretes;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CurrencyService;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CurrencyManager implements CurrencyService {
    private final String base;
    private final Map<String, Double> rates;
    public CurrencyManager(
            @Value("${app.fx.base}") String base,
            @Value("#{${app.fx.rates}}") Map<String, Double> rates
    ){ this.base = base; this.rates = rates; }

    @Override public Double toTRY(String currency, double amount){
        if(currency == null) return null;
        if("TRY".equalsIgnoreCase(currency)) return amount;
        Double r = rates.get(currency.toUpperCase());
        return (r==null)? null : amount * r;
    }
}