/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.metinkale.prayer;


import android.app.Activity;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.metinkale.prayerapp.utils.Geocoder;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.Main;
import com.metinkale.prayerapp.vakit.times.CalcTimes;
import com.metinkale.prayerapp.vakit.times.Source;
import com.metinkale.prayerapp.vakit.times.Times;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class VakitTest {

    @Rule
    public ActivityTestRule<Main> mActivityTestRule = new ActivityTestRule<>(Main.class);

    @org.junit.Test
    public void takeScreenshots() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }

        Activity act = mActivityTestRule.getActivity();
        String[] langs = act.getResources().getStringArray(R.array.language_val);
        act.finish();

        for (final String lang : langs) {
            Utils.changeLanguage(lang);
            Utils.init(act);
            mActivityTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    while (Times.getCount() > 0) {
                        Times.getTimes().get(0).delete();
                    }
                    createCityFor(lang);
                }
            });

            while (Times.getCount() == 0) {
                Thread.sleep(100);
            }


            act = mActivityTestRule.launchActivity(null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TestUtils.takeScreenshot("vakit" + lang.toUpperCase(), act);
            act.finish();
        }

    }

    private void createCityFor(final String lang) {
        final String city;
        switch (lang) {
            case "tr":
                city = "Kayseri";
                break;
            case "en":
                city = "London";
                break;
            case "nl":
                city = "Amsterdam";
                break;
            case "fr":
                city = "Paris";
                break;
            case "ku":
                city = "Türkiye, Diyarbakır";
                break;
            case "ar":
                city = "Mekke";
                break;
            case "ru":
                city = "Москва́";
                break;
            case "de":
            default:
                city = "Braunschweig";
                break;

        }
        Geocoder.search(city, new Geocoder.SearchCallback() {
            @Override
            public void onResult(List<Geocoder.Result> results) {
                Assert.assertFalse("empty geocoder result for \"" + city + "\"", results.isEmpty());

                Geocoder.Result i = results.get(0);

                CalcTimes t = new CalcTimes(System.currentTimeMillis());
                t.setSource(Source.Calc);
                t.setName(i.city);
                t.setLat(i.lat);
                t.setLng(i.lng);
                t.setMethod(Method.Makkah);
                t.setJuristic(Juristic.Hanafi);
                t.setAdjMethod(AdjMethod.AngleBased);
                t.setSortId(99);
            }

        });
    }


}
