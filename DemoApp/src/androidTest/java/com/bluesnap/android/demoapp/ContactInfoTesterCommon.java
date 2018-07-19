package com.bluesnap.android.demoapp;


import android.support.test.espresso.Espresso;

import com.bluesnap.androidapi.Constants;

import java.util.Arrays;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.bluesnap.android.demoapp.CardFormTesterCommon.cardNumberGeneratorTest;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;

/**
 * Created by sivani on 17/07/2018.
 */

public class ContactInfoTesterCommon {
    public static void empty_fields_invalid_error_validation(int componentResourceId, boolean fullInfo, boolean withEmail, int buttonComponent) throws InterruptedException {
        //String defaultCountry = BlueSnapService.getInstance().getUserCountry(this.mActivity.getApplicationContext());

        //Choosing brazil (that has state and zip)
        change_country(componentResourceId, "United States");

        //fix this- generalize to match shipping as well
        //Continue- leaving all fields empty
        onView(allOf(withId(R.id.buyNowButton), isDescendantOfA(withId(buttonComponent)))).perform(click());

        //verify error messages are displayed
        onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        if (withEmail)
            onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_email)),
                    isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        if (fullInfo) {
            onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_state)),
                    isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

            onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_city)),
                    isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

            //onView(withId(R.id.input_address)).perform(scrollTo());

            onView(allOf(withId(R.id.textinput_error), isDescendantOfA(withId(R.id.input_layout_address)),
                    isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));
        }

    }

    public static void name_invalid_error_validation(int componentResourceId, boolean withImeButton, int nextFieldResourceId) throws InterruptedException {
        //Click the field and leave it empty
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(click());
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering an invalid name- only one word
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(typeText("Sawyer"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering an invalid name- less than 2 characters
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(clearText(), typeText("L Fleur"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering an invalid name- less than 2 characters. BUG! waiting for it to be fixed
//        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(R.id.newShoppershippingViewComponent)))).perform(clearText(), typeText("La F"));
//        move_to_next_field(componentResourceId, false, nextFieldResourceId, 0);

        //Verify error message is displayed
//        onView(allOf(withId(R.id.textinput_error),
//                isDescendantOfA(withId(R.id.input_layout_name)),
//                isDescendantOfA(withId(R.id.newShoppershippingViewComponent)))).check(matches(isDisplayed()));

        //Entering an invalid name- spaces
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(clearText(), typeText("Sawyer     "));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering a valid name
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(clearText(), typeText("La Fleur"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(doesNotExist());

        //Entering an invalid name again- less than 2 characters
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(clearText(), typeText("L Fleur"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_name);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_name)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

    }

    public void email_invalid_error_validation(int componentResourceId, boolean withImeButton, int nextFieldResourceId) throws InterruptedException {
        //Click the field and leave it empty
        onView(withId(R.id.input_email)).perform(click());
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- without '@'
        onView(withId(R.id.input_email)).perform(typeText("broadwaydancecenter.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- without '.' finish
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter@gmail"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- too long suffix
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter@gmailgmailgmailgmailgmailgmail" +
                "gmailgmailgmailgmailgmailgmailgmailgmailgmailgmailgmailgmail.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- too long prefix1
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenterbroadwaydancecenterbroadwaydancecenter" +
                "broadwaydancecenterbroadwaydancecenterbroadwaydancecenterbroadwaydancecenterbroadwaydancecenter" +
                "broadwaydancecenterbroadwaydancecenterbroadwaydancecenterbroadwaydancecenter" +
                "broadwaydancecenterbroadwaydancecenter@gmail.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- too long prefix1
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter@gmail.comcom" +
                "comcomcomcomcomcomcom"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering an invalid email- illegal characters
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter*@gmail.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

        //Entering a valid email
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter@gmail.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(doesNotExist());

        //Entering an invalid email again- without '@'
        onView(withId(R.id.input_email)).perform(clearText(), typeText("broadwaydancecenter.com"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_email);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_email)))).check(matches(isDisplayed()));

    }

    public static void zip_invalid_error_validation(int componentResourceId, boolean withImeButton, int nextFieldResourceId) throws InterruptedException {
        //fill in country with zip
        onView(allOf(withId(R.id.countryImageButton), isDescendantOfA(withId(componentResourceId)))).perform(click());
        onData(hasToString(containsString("Israel"))).inAdapterView(withId(R.id.country_list_view)).perform(click());

        //Click the field and leave it empty
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId)))).perform(click());
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering an invalid zip- invalid characters
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId))))
                .perform(typeText("12345*"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering a valid zip- only numbers
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("12345"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(doesNotExist());

        //Entering a valid zip- with characters
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("12345abcde"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(doesNotExist());

        //Entering a valid zip- with spaces
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("12345 abcde"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(doesNotExist());

        //Entering an invalid zip again- invalid characters
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId))))
                .perform(typeText("12345%"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_zip);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_zip)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));
    }

    public static void city_invalid_error_validation(int componentResourceId, boolean withImeButton, int nextFieldResourceId) throws InterruptedException {
        //Click the field and leave it empty
        onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId)))).perform(click());
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_city);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_city)),
                isDescendantOfA(withId(componentResourceId))))
                .perform(scrollTo()).check(matches(isDisplayed()));

        //Entering an invalid city- less then 2 characters
        onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId))))
                .perform(typeText("a"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_city);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_city)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering an invalid zip- spaces
        onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("        "));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_city);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_city)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

        //Entering a valid zip- with characters
        onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("New York"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_city);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_city)),
                isDescendantOfA(withId(componentResourceId)))).check(doesNotExist());

        //Entering an invalid city- less then 2 characters
        onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("a"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_city);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_city)),
                isDescendantOfA(withId(componentResourceId)))).check(matches(isDisplayed()));

    }

    public static void address_invalid_error_validation(int componentResourceId, boolean withImeButton, int nextFieldResourceId) throws InterruptedException {
        //Click the field and leave it empty
        onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId)))).perform(scrollTo(), click());
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_address);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_address)),
                isDescendantOfA(withId(componentResourceId))))
                .perform(scrollTo()).check(matches(isDisplayed()));

        //Entering an invalid city- less then 2 characters
        onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId))))
                .perform(typeText("a"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_address);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_address)),
                isDescendantOfA(withId(componentResourceId))))
                .perform(scrollTo()).check(matches(isDisplayed()));

        //Entering an invalid zip- spaces
        onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("        "));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_address);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_address)),
                isDescendantOfA(withId(componentResourceId))))
                .perform(scrollTo()).check(matches(isDisplayed()));

        //Entering a valid zip- with characters
        onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("New York"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_address);

        //Verify error message is not displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_address)),
                isDescendantOfA(withId(componentResourceId))))
                .check(doesNotExist());

        //Entering an invalid city- less then 2 characters
        onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId))))
                .perform(clearText(), typeText("a"));
        move_to_next_field(componentResourceId, withImeButton, nextFieldResourceId, R.id.input_address);

        //Verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_address)),
                isDescendantOfA(withId(componentResourceId))))
                .perform(scrollTo()).check(matches(isDisplayed()));
    }

    public static void state_invalid_error(int componentResourceId, int buttonComponent) throws InterruptedException {
        //Choosing brazil (that has state)
        onView(allOf(withId(R.id.countryImageButton), isDescendantOfA(withId(componentResourceId)))).perform(click());
        onData(hasToString(containsString("Brazil"))).inAdapterView(withId(R.id.country_list_view)).perform(click());

        //Try to pay without filling in state
        onView(allOf(withId(R.id.buyNowButton), isDescendantOfA(withId(buttonComponent)))).perform(click());

        //Espresso.closeSoftKeyboard();
        //verify error message is displayed
        onView(allOf(withId(R.id.textinput_error),
                isDescendantOfA(withId(R.id.input_layout_state)))).check(matches(isDisplayed()));

        //filling in Rio de Janeiro
        onView(allOf(withId(R.id.input_state), isDescendantOfA(withId(componentResourceId)))).perform(click());
        onData(hasToString(containsString("Rio de Janeiro"))).inAdapterView(withId(R.id.state_list_view)).perform(click());
        Espresso.closeSoftKeyboard();

        //waiting for this bug to be fixed
//        //verify error message is not displayed anymore
//        onView(allOf(withId(R.id.textinput_error),
//                isDescendantOfA(withId(R.id.input_layout_state)))).check(matches(not(isDisplayed())));
    }

    //Fix this. check if it is possible to handle all cases in this one general function
    public void contact_info_saved_validation(int componentResourceId, boolean fullInfo, boolean withEmail) throws InterruptedException {
        //Changing country to USA to have state
        change_country(componentResourceId, "United States");

        CardFormTesterCommon.fillInContactInfoShipping("US");

        //Verify country has been saved in billing
        onView(allOf(withId(R.id.countryImageButton), isDescendantOfA(withId(componentResourceId)))).check(matches(TestUtils.withDrawable(R.drawable.us)));

        //Verify full name has been saved in billing
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).check(matches(withText("La Fleur")));

        if (withEmail)//Verify email has been saved in billing
            onView(withId(R.id.input_email)).check(matches(withText("test@sdk.com")));

        //Verify zip has been saved in billing
        onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId)))).check(matches(withText("3abc 324a")));

        if (fullInfo) {
            //Verify state has been saved in billing
            onView(allOf(withId(R.id.input_state), isDescendantOfA(withId(componentResourceId)))).check(matches(withText("NY")));

            //Verify city has been saved in billing
            onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId)))).check(matches(withText("Tel Aviv")));

            //Verify address has been saved in billing
            onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId)))).check(matches(withText("Rotchild street")));
        }
    }

    public static void check_filling_in_cc_info_flow() {
        onView(withId(R.id.creditCardNumberEditText)).perform(typeText(cardNumberGeneratorTest()));
        onView(withId(R.id.expEditText)).check(matches(TestUtils.isViesFocused()));

        onView(withId(R.id.expEditText)).perform(typeText("12 26"));
        onView(withId(R.id.cvvEditText)).check(matches(TestUtils.isViesFocused()));

        onView(withId(R.id.cvvEditText)).perform(typeText("123"));
    }

    public static void check_ime_action_button_in_cc_info() {
        onView(withId(R.id.creditCardNumberEditText)).perform(click(), pressImeActionButton());
//        onView(withId(R.id.expEditText)).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());
//        onView(withId(R.id.cvvEditText)).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());
        onView(withId(R.id.input_name)).check(matches(TestUtils.isViesFocused()));
    }

    public static void check_ime_action_button_in_contact_info(String country, int componentResourceId, boolean fullInfo, boolean withEmail) {
        onView(allOf(withId(R.id.input_name), isDescendantOfA(withId(componentResourceId)))).perform(click(), pressImeActionButton());
        if (withEmail)
            onView(withId(R.id.input_email)).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());
        if (!Arrays.asList(Constants.COUNTRIES_WITHOUT_ZIP).contains(country))
            onView(allOf(withId(R.id.input_zip), isDescendantOfA(withId(componentResourceId)))).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());

        if (fullInfo) {
            onView(allOf(withId(R.id.input_city), isDescendantOfA(withId(componentResourceId)))).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());
            onView(allOf(withId(R.id.input_address), isDescendantOfA(withId(componentResourceId)))).check(matches(TestUtils.isViesFocused())).perform(pressImeActionButton());
        }
    }

    public static void continue_to_shipping(String country, boolean fullInfo, boolean withEmail) {
        CardFormTesterCommon.fillInCCLineWithValidCard();
        CardFormTesterCommon.fillInContactInfoBilling(country, fullInfo, withEmail);

        onView(withId(R.id.buyNowButton)).perform(click());
    }

    private static void move_to_next_field(int componentResourceId, boolean withImeButton, int nextFieldResourceId, int currFieldResourceId) {
        if (withImeButton)
            onView(allOf(withId(currFieldResourceId), isDescendantOfA(withId(componentResourceId)))).perform(pressImeActionButton());
        else
            onView(allOf(withId(nextFieldResourceId), isDescendantOfA(withId(componentResourceId)))).perform(scrollTo(), click());
    }

    private static void change_country(int componentResourceId, String country) {
        onView(allOf(withId(R.id.countryImageButton), isDescendantOfA(withId(componentResourceId)))).perform(click());
        onData(hasToString(containsString(country))).inAdapterView(withId(R.id.country_list_view)).perform(click());
    }

}
