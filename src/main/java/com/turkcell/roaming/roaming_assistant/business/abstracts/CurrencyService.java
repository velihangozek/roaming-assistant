package com.turkcell.roaming.roaming_assistant.business.abstracts;

public interface CurrencyService {
    Double toTRY(String currency, double amount); // null ise çevirmeyiz
}