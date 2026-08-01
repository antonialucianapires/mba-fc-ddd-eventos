package com.mba.fc.ingressos.events.partners;

import com.mba.fc.ingressos.core.events.application.PartnerService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/partners")
public class PartnersController {

    private final PartnerService partnerService;

    public PartnersController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    public Set<PartnerResponse> getPartners() {
        return partnerService.list().stream().map(PartnerResponse::new).collect(Collectors.toSet());
    }

    @PostMapping
    public PartnerResponse createPartner(@RequestBody PartnerRequest request) {
        return new PartnerResponse(partnerService.create(request.name()));
    }
}
