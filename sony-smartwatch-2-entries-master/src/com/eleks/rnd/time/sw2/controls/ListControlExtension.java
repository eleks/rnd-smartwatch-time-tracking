/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.eleks.rnd.time.sw2.controls;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.eleks.rnd.time.sw2.AdvancedLayoutsExtensionService;
import com.eleks.rnd.time.sw2.R;
import com.eleks.rnd.time.sw2.api.Hardcoded;
import com.eleks.rnd.time.sw2.api.Hardcoded.TimerListener;
import com.eleks.rnd.time.sw2.api.TimeEntry;
import com.eleks.rnd.time.sw2.utils.UIBundle;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

/**
 * ListControlExtension displays a scrollable list, based on a string array.
 * Tapping on list items opens a swipable detail view.
 */
public class ListControlExtension extends ManagedControlExtension {

    protected int mLastKnowPosition = 0;
    private TimerListener mTimerListener = null;

    /**
     * @see ManagedControlExtension#ManagedControlExtension(Context, String,
     *      ControlManagerCostanza, Intent)
     */
    public ListControlExtension(Context context, String hostAppPackageName, ControlManagerSmartWatch2 controlManager, Intent intent) {
        super(context, hostAppPackageName, controlManager, intent);
        Log.d(AdvancedLayoutsExtensionService.LOG_TAG, "ListControl constructor");
    }

    @Override
    public void onResume() {
        Log.d(AdvancedLayoutsExtensionService.LOG_TAG, "onResume");
        showLayout(R.layout.layout_test_list, null);
        sendListCount(R.id.listView, Hardcoded.DATA.getEntries().size());

        // If requested, move to the correct position in the list.
        int startPosition = getIntent().getIntExtra(GalleryTestControl.EXTRA_INITIAL_POSITION, 0);
        mLastKnowPosition = startPosition;
        sendListPosition(R.id.listView, startPosition);
        
        mTimerListener = new TimerListener() {
            @Override
            public void onTimerStopped(String id) {
                ControlListItem item = updateControlListItemTimer(Integer.parseInt(id), false);
                if (item != null) {
                    sendListItem(item);
                    startVibrator(300, 0, 1);
                }
            }
            
            @Override
            public void onTimerStarted(String id) {
                ControlListItem item = updateControlListItemTimer(Integer.parseInt(id), true);
                if (item != null) {
                    sendListItem(item);
                    startVibrator(600, 0, 1);
                }
            }
        }; 
        Hardcoded.DATA.setTimerListener(mTimerListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Hardcoded.DATA.setTimerListener(null);
        // Position is saved into Control's Intent, possibly to be used later.
        getIntent().putExtra(GalleryTestControl.EXTRA_INITIAL_POSITION, mLastKnowPosition);
    }

    @Override
    public void onRequestListItem(final int layoutReference, final int listItemPosition) {
//        Log.d(AdvancedLayoutsExtensionService.LOG_TAG, "onRequestListItem() - position " + listItemPosition);
        if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.listView) {
            ControlListItem item = createControlListItem(listItemPosition);
            if (item != null) {
                sendListItem(item);
            }
        }
    }

    @Override
    public void onListItemSelected(ControlListItem listItem) {
        super.onListItemSelected(listItem);
        // We save the last "selected" position, this is the current visible
        // list item index. The position can later be used on resume
        mLastKnowPosition = listItem.listItemPosition;
    }

    @Override
    public void onListItemClick(final ControlListItem listItem, final int clickType, final int itemLayoutReference) {
        Log.d(AdvancedLayoutsExtensionService.LOG_TAG, "Item clicked. Position " + listItem.listItemPosition + ", itemLayoutReference " + itemLayoutReference
                + ". Type was: " + (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT" : "LONG"));

        if (clickType == Control.Intents.CLICK_TYPE_SHORT) {
            Intent intent = new Intent(mContext, TimeEntryControl.class);
            // Here we pass the item position to the next control. It would
            // also be possible to put some unique item id in the list item and
            // pass listItem.listItemId here.
            intent.putExtra(TimeEntryControl.EXTRA_ENTRY_ID, String.valueOf(listItem.listItemPosition));
            mControlManager.startControl(intent);
        } else if (clickType == Control.Intents.CLICK_TYPE_LONG) {
            Hardcoded.DATA.toggleTimer(String.valueOf(listItem.listItemPosition));
        }
    }

    /**
     * Creates a list item containing an icon, a title and a body text.
     * 
     * @param position
     *            The position of the item in the list.
     * @return The list item.
     */
    protected ControlListItem createControlListItem(int position) {

        ControlListItem item = new ControlListItem();
        item.layoutReference = R.id.listView;
        item.dataXmlLayout = R.layout.item_list;
        item.listItemPosition = position;
        item.listItemId = position;

        TimeEntry entry = Hardcoded.DATA.getEntries().get(position);

        int icon = Hardcoded.DATA.isTimer(entry) 
                ? R.drawable.timer_green
                : R.drawable.time_entry_item;
        
        Bundle[] bundleData = UIBundle.with()
                .uri(R.id.thumbnail, ExtensionUtils.getUriString(mContext, icon))
                .text(R.id.client, entry.getClient())
                .text(R.id.matter, entry.getMatter())
                .bundle();
        
        item.layoutData = bundleData;

        return item;
    }
    
    protected ControlListItem updateControlListItemTimer(int position, boolean timerOn) {

        Log.d(AdvancedLayoutsExtensionService.LOG_TAG, "updateControlListItem pos. " + position + " timer on: " + timerOn);
        
        ControlListItem item = new ControlListItem();
        item.layoutReference = R.id.listView;
        item.dataXmlLayout = R.layout.item_list;
        item.listItemPosition = position;
        // We use position as listItemId. Here we could use some other unique id
        // to reference the list data
        item.listItemId = position;

        TimeEntry entry = Hardcoded.DATA.getEntries().get(position);

        int icon = timerOn ? R.drawable.timer_green : R.drawable.time_entry_item;
        
        Bundle[] bundleData = UIBundle.with()
                .uri(R.id.thumbnail, ExtensionUtils.getUriString(mContext, icon))
                .text(R.id.client, entry.getClient())
                .text(R.id.matter, entry.getMatter())
                .bundle();
        
        item.layoutData = bundleData;

        return item;
    }

}
