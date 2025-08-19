package com.turkcell.roaming.roaming_assistant.business.abstracts;

import com.turkcell.roaming.roaming_assistant.dto.requests.SimulateRequest;
import com.turkcell.roaming.roaming_assistant.dto.responses.SimulateResponse;
public interface SimulationService { SimulateResponse simulate(SimulateRequest req); }