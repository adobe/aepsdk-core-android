package com.adobe.marketing.mobile.identity;

import com.adobe.marketing.mobile.services.DataEntity;
import com.adobe.marketing.mobile.services.HitProcessing;

public class IdentityHitsProcessing implements HitProcessing {
    @Override
    public int retryInterval(DataEntity entity) {
        return 0;
    }

    @Override
    public boolean processHit(DataEntity entity) {
        return false;
    }
}
