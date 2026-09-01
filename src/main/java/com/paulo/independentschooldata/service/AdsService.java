package com.paulo.independentschooldata.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdsService {

    private boolean isAdsEnabled = false;

    public boolean getCurrentStateOfAdds() {
        log.info("Current state of adsEnabled is {}", isAdsEnabled);
        return isAdsEnabled;
    }

    public boolean enable() {
        isAdsEnabled = true;
        log.info("Current state of adsEnabled is {}", isAdsEnabled);
        return isAdsEnabled;
    }

    public boolean disable() {
        isAdsEnabled = false;
        log.info("Current state of adsEnabled is {}", isAdsEnabled);
        return isAdsEnabled;
    }
}
