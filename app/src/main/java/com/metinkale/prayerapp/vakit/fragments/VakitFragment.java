/*
 * Copyright (c) 2013-2017 Metin Kale
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
 */

package com.metinkale.prayerapp.vakit.fragments;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.MainActivity;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.utils.MultipleOrientationSlidingDrawer;
import com.metinkale.prayerapp.utils.RTLViewPager;
import com.metinkale.prayerapp.utils.Utils;
import com.metinkale.prayerapp.vakit.times.Times;

public class VakitFragment extends MainActivity.MainFragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public MyAdapter mAdapter;
    public FloatingActionButton mAddCityFab;
    private RTLViewPager mPager;
    private int mStartPos = 1;
    private SettingsFragment mSettingsFrag;
    private ImsakiyeFragment mImsakiyeFrag;
    private TextView mFooterText;
    private MultipleOrientationSlidingDrawer mTopSlider;
    private MultipleOrientationSlidingDrawer mBottomSlider;

    @Nullable
    public static PendingIntent getPendingIntent(@Nullable Times t) {
        if (t == null) {
            return null;
        }
        Context context = App.get();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("startCity", Times.getTimes().indexOf(t));
        return PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public MultipleOrientationSlidingDrawer getTopSlider() {
        return mTopSlider;
    }

    public MultipleOrientationSlidingDrawer getBottomSlider() {
        return mBottomSlider;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.vakit_main, container, false);
        mFooterText = v.findViewById(R.id.footerText);
        mPager = v.findViewById(R.id.pager);
        mAdapter = new MyAdapter(getChildFragmentManager());

        mSettingsFrag = new SettingsFragment();
        mImsakiyeFrag = new ImsakiyeFragment();

        getChildFragmentManager().beginTransaction()
                .replace(R.id.imsakiyeContainer, mImsakiyeFrag)
                .replace(R.id.settingsContainer, mSettingsFrag)
                .commit();


        mTopSlider = v.findViewById(R.id.topSlider);
        mBottomSlider = v.findViewById(R.id.bottomSlider);

        mAddCityFab = v.findViewById(R.id.addCity);
        mAddCityFab.setOnClickListener(this);

        mPager.setRTLSupportAdapter(getChildFragmentManager(), mAdapter);

        int holyday = HicriDate.isHolyday();
        if (holyday != 0) {
            TextView tv = v.findViewById(R.id.holyday);
            tv.setVisibility(View.VISIBLE);
            tv.setText(Utils.getHolyday(holyday - 1));
        }

        mPager.addOnPageChangeListener(this);


        mTopSlider.setOnDrawerScrollListener(new MultipleOrientationSlidingDrawer.OnDrawerScrollListener() {
            @Override
            public void onScrollStarted() {
            }

            @Override
            public void onScrolling(int pos) {
                mPager.setTranslationY(pos);
            }

            @Override
            public void onScrollEnded() {

            }
        });

        mTopSlider.setOnDrawerOpenListener(new MultipleOrientationSlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                int position = mPager.getCurrentItem();
                if (position != 0) {
                    Times t = Times.getTimes(mAdapter.getItemId(position));
                    mSettingsFrag.setTimes(t);
                }
                mBottomSlider.lock();
            }
        });

        mTopSlider.setOnDrawerCloseListener(new MultipleOrientationSlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mBottomSlider.unlock();

                Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + mPager.getId() + ":" + mAdapter.getItemId(mPager.getCurrentItem()));
                if (page instanceof CityFragment) {
                    ((CityFragment) page).update();
                }
            }
        });


        mBottomSlider.setOnDrawerOpenListener(new MultipleOrientationSlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                mTopSlider.lock();
            }
        });

        mBottomSlider.setOnDrawerCloseListener(new MultipleOrientationSlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mTopSlider.unlock();
            }
        });
        v.findViewById(R.id.topSliderCloseHandler).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mTopSlider.isOpened()) {
                    mTopSlider.animateClose();
                    return true;
                }
                return false;
            }
        });


        v.findViewById(R.id.bottomSliderCloseHandler).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && mBottomSlider.isOpened()) {
                    mBottomSlider.animateClose();
                    return true;
                }
                return false;
            }
        });

        if (getArguments() != null) {
            mStartPos = getArguments().getInt("startCity", -1) + 1;
        } else mStartPos = 1;

        if (mStartPos <= 0) {
            mStartPos = 1;
        }
        mStartPos = Math.min(mStartPos, mAdapter.getCount() - 1);

        mPager.setCurrentItem(mStartPos);
        onPageScrolled(mStartPos, 0, 0);
        onPageSelected(mStartPos);
        onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);

        return v;
    }


    @Override
    public void onClick(View v) {
        if (v == mAddCityFab) {
            moveToFrag(new SearchCityFragment());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Times.addOnTimesListChangeListener(mAdapter);


    }

    @Override
    public void onPause() {
        super.onPause();
        Times.removeOnTimesListChangeListener(mAdapter);
    }


    @Override
    public boolean onBackPressed() {
        Fragment frag = getChildFragmentManager().findFragmentByTag("notPrefs");
        if (frag != null) {
            setFooterText(getString(R.string.monthly), true);
            getChildFragmentManager().beginTransaction().remove(frag).commit();
            return true;
        } else if (mBottomSlider.isOpened()) {
            mBottomSlider.animateClose();
            return true;
        } else if (mTopSlider.isOpened()) {
            mTopSlider.animateClose();
            return true;
        }
        return false;
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int pos = mPager.getCurrentItem();
            if (pos != 0) {
                Times t = Times.getTimes(mAdapter.getItemId(pos));
                mImsakiyeFrag.setTimes(t);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0) {
            mTopSlider.lock();
            mBottomSlider.lock();
        } else {
            mTopSlider.unlock();
            mBottomSlider.unlock();
        }
        if (mAddCityFab != null) if ((position == 0) && (positionOffset == 0)) {
            mAddCityFab.show();
        } else {
            mAddCityFab.hide();
        }


    }

    public void setFooterText(@NonNull CharSequence txt, boolean enablePane) {
        mFooterText.setVisibility(txt.toString().isEmpty() ? View.GONE : View.VISIBLE);
        mFooterText.setText(txt);
        mPager.setSwipeLocked(!enablePane);

    }

    @Override
    public void onPageSelected(int pos) {
        mFooterText.setText((pos == 0) ? R.string.cities : R.string.monthly);
    }

    public void onItemClick(int pos) {
        mPager.setCurrentItem(pos + 1, true);

    }

    public class MyAdapter extends FragmentPagerAdapter implements Times.OnTimesListChangeListener {

        MyAdapter(FragmentManager fm) {
            super(fm);

        }


        @Override
        public int getCount() {
            return Times.getCount() + 1;
        }

        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return 0;
            }

            return Times.getTimesAt(position - 1).getID();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof SortFragment) {
                return 0;
            } else {
                CityFragment frag = (CityFragment) object;
                int pos = Times.getTimes().indexOf(frag.getTimes());
                if (pos >= 0) {
                    return pos + 1;
                }
            }
            return POSITION_NONE;
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position > 0) {
                CityFragment frag = new CityFragment();
                Bundle bdl = new Bundle();
                bdl.putLong("city", getItemId(position));
                frag.setArguments(bdl);

                if (position == mStartPos) {
                    mStartPos = 0;
                }
                return frag;
            } else {
                return new SortFragment();
            }
        }


    }
}
