/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.settings.datausage;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.util.RecurrenceRule;

import com.android.car.settings.CarSettingsRobolectricTestRunner;

import com.google.android.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CarSettingsRobolectricTestRunner.class)
public class DataUsageUtilsTest {

    private static final int SUBSCRIPTION_ID = 1;

    @Test
    public void getPrimaryPlan_noSubscriptions_returnsNull() {
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        when(subscriptionManager.getSubscriptionPlans(SUBSCRIPTION_ID)).thenReturn(
                Lists.newArrayList());

        assertThat(DataUsageUtils.getPrimaryPlan(subscriptionManager, SUBSCRIPTION_ID)).isNull();

    }

    @Test
    public void getPrimaryPlan_dataLimitBytesIsZero_returnsNull() {
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        SubscriptionPlan subscriptionPlan = mock(SubscriptionPlan.class);
        when(subscriptionManager.getSubscriptionPlans(SUBSCRIPTION_ID)).thenReturn(
                Lists.newArrayList(subscriptionPlan));
        when(subscriptionPlan.getDataLimitBytes()).thenReturn(0L);

        assertThat(DataUsageUtils.getPrimaryPlan(subscriptionManager, SUBSCRIPTION_ID)).isNull();

    }

    @Test
    public void getPrimaryPlan_dataUsageBytesIsHuge_returnsNull() {
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        SubscriptionPlan subscriptionPlan = mock(SubscriptionPlan.class);
        when(subscriptionManager.getSubscriptionPlans(SUBSCRIPTION_ID)).thenReturn(
                Lists.newArrayList(subscriptionPlan));
        when(subscriptionPlan.getDataLimitBytes()).thenReturn(100L);
        when(subscriptionPlan.getDataUsageBytes()).thenReturn(2 * DataUsageUtils.PETA);

        assertThat(DataUsageUtils.getPrimaryPlan(subscriptionManager, SUBSCRIPTION_ID)).isNull();
    }

    @Test
    public void getPrimaryPlan_cycleRuleIsNull_returnsNull() {
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        SubscriptionPlan subscriptionPlan = mock(SubscriptionPlan.class);
        when(subscriptionManager.getSubscriptionPlans(SUBSCRIPTION_ID)).thenReturn(
                Lists.newArrayList(subscriptionPlan));
        when(subscriptionPlan.getDataLimitBytes()).thenReturn(100L);
        when(subscriptionPlan.getDataUsageBytes()).thenReturn(10L);
        when(subscriptionPlan.getCycleRule()).thenReturn(null);

        assertThat(DataUsageUtils.getPrimaryPlan(subscriptionManager, SUBSCRIPTION_ID)).isNull();
    }

    @Test
    public void getPrimaryPlan_cycleRuleIsValid_returnsSubscriptionPlan() {
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        SubscriptionPlan subscriptionPlan = mock(SubscriptionPlan.class);
        RecurrenceRule recurrenceRule = mock(RecurrenceRule.class);
        when(subscriptionManager.getSubscriptionPlans(SUBSCRIPTION_ID)).thenReturn(
                Lists.newArrayList(subscriptionPlan));
        when(subscriptionPlan.getDataLimitBytes()).thenReturn(100L);
        when(subscriptionPlan.getDataUsageBytes()).thenReturn(10L);
        when(subscriptionPlan.getCycleRule()).thenReturn(recurrenceRule);

        assertThat(DataUsageUtils.getPrimaryPlan(subscriptionManager, SUBSCRIPTION_ID)).isEqualTo(
                subscriptionPlan);
    }
}