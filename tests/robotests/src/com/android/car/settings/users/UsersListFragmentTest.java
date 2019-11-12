/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.settings.users;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.car.userlib.CarUserManagerHelper;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.view.View;
import android.widget.Button;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.R;
import com.android.car.settings.common.ConfirmationDialogFragment;
import com.android.car.settings.testutils.BaseTestActivity;
import com.android.car.settings.testutils.ShadowCarUserManagerHelper;
import com.android.car.settings.testutils.ShadowUserIconProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

/**
 * Tests for UserDetailsFragment.
 */
@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowCarUserManagerHelper.class, ShadowUserIconProvider.class})
public class UsersListFragmentTest {

    private Context mContext;
    private BaseTestActivity mTestActivity;
    private UsersListFragment mFragment;
    private Button mActionButton;
    private ConfirmationDialogFragment mDialog;

    @Mock
    private CarUserManagerHelper mCarUserManagerHelper;
    @Mock
    private UserManager mUserManager;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowCarUserManagerHelper.setMockInstance(mCarUserManagerHelper);
        mContext = RuntimeEnvironment.application;
        mTestActivity = Robolectric.setupActivity(BaseTestActivity.class);
    }

    @After
    public void tearDown() {
        ShadowCarUserManagerHelper.reset();
    }

    @Test
    public void onCreate_userInDemoMode_showsExitRetailModeButton() {
        when(mCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(true);

        createUsersListFragment();

        assertThat(mActionButton.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mActionButton.getText().toString())
            .isEqualTo(mContext.getString(R.string.exit_retail_button_text));
    }

    @Test
    public void onCreate_userCanAddNewUser_showsAddUserButton() {
        when(mCarUserManagerHelper.canCurrentProcessAddUsers()).thenReturn(true);

        createUsersListFragment();

        assertThat(mActionButton.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mActionButton.getText().toString())
            .isEqualTo(mContext.getString(R.string.user_add_user_menu));
    }

    @Test
    public void onCreate_userRestrictedFromAddingNewUserAndNotInDemo_doesNotShowActionButton() {
        when(mCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(false);
        when(mCarUserManagerHelper.canCurrentProcessAddUsers()).thenReturn(false);

        createUsersListFragment();

        assertThat(mActionButton.getVisibility()).isEqualTo(View.GONE);
    }

    /* Test that onCreateNewUserConfirmed invokes a creation of a new non-admin. */
    @Test
    public void testOnCreateNewUserConfirmedInvokesCreateNewNonAdminUser() {
        createUsersListFragment();
        mFragment.mConfirmListener.onConfirm(/* arguments= */ null);
        Robolectric.flushBackgroundThreadScheduler();
        verify(mCarUserManagerHelper)
                .createNewNonAdminUser(mContext.getString(R.string.user_new_user_name));
    }

    /* Test that if we're in demo user, click on the button starts exit out of the retail mode. */
    @Test
    public void testCallOnClick_demoUser_exitRetailMode() {
        when(mCarUserManagerHelper.isCurrentProcessDemoUser()).thenReturn(true);
        createUsersListFragment();
        mActionButton.callOnClick();
        assertThat(isDialogShown(ConfirmExitRetailModeDialog.DIALOG_TAG)).isTrue();
    }

    /* Test that if the max num of users is reached, click on the button informs user of that. */
    @Test
    public void testCallOnClick_userLimitReached_showErrorDialog() {
        when(mCarUserManagerHelper.getMaxSupportedRealUsers()).thenReturn(5);
        when(mCarUserManagerHelper.isUserLimitReached()).thenReturn(true);
        createUsersListFragment();

        mActionButton.callOnClick();
        assertThat(isDialogShown(MaxUsersLimitReachedDialog.DIALOG_TAG)).isTrue();
    }

    /* Test that if user can add other users, click on the button creates a dialog to confirm. */
    @Test
    public void testCallOnClick_showAddUserDialog() {
        when(mCarUserManagerHelper.canCurrentProcessAddUsers()).thenReturn(true);
        createUsersListFragment();

        mActionButton.callOnClick();
        assertThat(isDialogShown(ConfirmationDialogFragment.TAG)).isTrue();
    }

    private void createUsersListFragment() {
        UserInfo testUser = new UserInfo();
        mFragment = new UsersListFragment();
        when(mCarUserManagerHelper.getCurrentProcessUserInfo()).thenReturn(testUser);
        when(mUserManager.getUserInfo(anyInt())).thenReturn(testUser);
        when(mCarUserManagerHelper.getAllSwitchableUsers()).thenReturn(new ArrayList<UserInfo>());
        when(mCarUserManagerHelper.createNewNonAdminUser(any())).thenReturn(null);
        mTestActivity.launchFragment(mFragment);
        refreshButtons();
    }

    private void refreshButtons() {
        mActionButton = (Button) mTestActivity.findViewById(R.id.action_button1);
    }

    private boolean isDialogShown(String tag) {
        return mTestActivity.getSupportFragmentManager().findFragmentByTag(tag) != null;
    }
}
