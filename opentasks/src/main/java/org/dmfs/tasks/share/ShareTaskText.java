/*
 * Copyright 2017 dmfs GmbH
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

package org.dmfs.tasks.share;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import org.dmfs.provider.tasks.TaskContract;
import org.dmfs.tasks.R;
import org.dmfs.tasks.model.CheckListItem;
import org.dmfs.tasks.model.ContentSet;
import org.dmfs.tasks.model.Model;
import org.dmfs.tasks.model.TaskFieldAdapters;
import org.dmfs.tasks.model.adapters.TimeZoneWrapper;
import org.dmfs.tasks.utils.DateFormatter;
import org.dmfs.tasks.utils.charsequence.AbstractCharSequence;
import org.dmfs.tasks.utils.factory.Factory;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import au.com.codeka.carrot.CarrotEngine;
import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;

/*
 <task title>
 ============

 <task description>
 [X] checked list item
 [ ] unchecked list item

 Location: <location>
 Start: <start date time> <timezone>
 Due: <due date time> <timezone>
 Completed: <due date time> <timezone>
 Priority: <priority text>
 Privacy: <privacy text>
 Status: <status text>
 <url>

 --
 Shared by OpenTasks
 */


/**
 * {@link CharSequence} for sharing task information, uses <code>carrot</code> template engine.
 *
 * @author Gabor Keszthelyi
 */
public class ShareTaskText extends AbstractCharSequence
{
    public ShareTaskText(final ContentSet contentSet, final Model model, final Context context)
    {
        super(new OutputFactory(contentSet, model, context));
    }


    private static final class OutputFactory implements Factory<CharSequence>
    {
        private final ContentSet mContentSet;
        private final Model mModel;
        private final Context mContext;


        public OutputFactory(ContentSet contentSet, Model model, Context context)
        {
            mContentSet = contentSet;
            mModel = model;
            mContext = context;
        }


        @Override
        public CharSequence create()
        {
            CarrotEngine engine = new CarrotEngine();
            Configuration config = engine.getConfig();
            config.setResourceLocater(new RawIdResourceLocator(mContext));
            try
            {
                String templateName = String.valueOf(R.raw.sharetask);
                String output = engine.process(templateName, bindings());
                Log.v("ShareTaskText", output);
                return output;
            }
            catch (CarrotException e)
            {
                throw new RuntimeException("Failed to process template with carrot", e);
            }
        }


        private Map<String, Object> bindings()
        {
            Map<String, Object> bindings = new TreeMap<>();

            // Title
            bindings.put("title", TaskFieldAdapters.TITLE.get(mContentSet));

            // Description
            String description = TaskFieldAdapters.DESCRIPTION.get(mContentSet);
            bindings.put("hasDescription", !TextUtils.isEmpty(description));
            bindings.put("description", description);

            // Checklist items
            List<CheckListItem> checkListItems = TaskFieldAdapters.CHECKLIST.get(mContentSet);
            boolean hasCheckListItems = checkListItems != null && !checkListItems.isEmpty();
            bindings.put("hasCheckListItems", hasCheckListItems);
            bindings.put("checkListItems", checkListItems);

            // Location
            String location = TaskFieldAdapters.LOCATION.get(mContentSet);
            bindings.put("hasLocation", !TextUtils.isEmpty(location));
            bindings.put("location", location);

            // Start time
            Time startTime = TaskFieldAdapters.DTSTART.get(mContentSet);
            boolean hasStart = startTime != null;
            bindings.put("hasStart", hasStart);
            if (hasStart)
            {
                bindings.put("startDateTime", formatTime(startTime));
            }

            // Due time
            Time dueTime = TaskFieldAdapters.DUE.get(mContentSet);
            boolean hasDue = dueTime != null;
            bindings.put("hasDue", hasDue);
            if (hasDue)
            {
                bindings.put("dueDateTime", formatTime(dueTime));
            }

            // Completed time
            Time completedTime = TaskFieldAdapters.COMPLETED.get(mContentSet);
            boolean hasCompleted = completedTime != null;
            bindings.put("hasCompleted", completedTime != null);
            if (hasCompleted)
            {
                bindings.put("completedDateTime", formatTime(completedTime));
            }

            // Priority
            Integer priority = TaskFieldAdapters.PRIORITY.get(mContentSet);
            boolean hasPriority = priority != null;
            bindings.put("hasPriority", hasPriority);
            if (hasPriority)
            {
                String priorityText = mModel.getField(R.id.task_field_priority).getChoices().getTitle(priority);
                bindings.put("priority", priorityText);
            }

            // Privacy
            Integer classification = TaskFieldAdapters.CLASSIFICATION.get(mContentSet);
            boolean hasPrivacy = classification != null;
            bindings.put("hasPrivacy", hasPrivacy);
            if (hasPrivacy)
            {
                String classificationText = mModel.getField(R.id.task_field_classification)
                        .getChoices()
                        .getTitle(classification);
                bindings.put("privacy", classificationText);
            }

            // Status
            Integer status = TaskFieldAdapters.STATUS.get(mContentSet);
            boolean hasStatus = status != null && !status.equals(TaskContract.Tasks.STATUS_COMPLETED);
            bindings.put("hasStatus", hasStatus);
            if (hasStatus)
            {
                String statusText = mModel.getField(R.id.task_field_status).getChoices().getTitle(status);
                bindings.put("status", statusText);
            }

            // Url
            URL url = TaskFieldAdapters.URL.get(mContentSet);
            boolean hasUrl = url != null;
            bindings.put("hasUrl", hasUrl);
            if (hasUrl)
            {
                bindings.put("url", url);
            }

            return bindings;
        }


        private String formatTime(Time time)
        {
            String dateTimeText = new DateFormatter(mContext).format(time, DateFormatter.DateFormatContext.DETAILS_VIEW);
            TimeZone timeZone = TaskFieldAdapters.TIMEZONE.get(mContentSet);
            if (timeZone == null)
            {
                return dateTimeText;
            }
            else
            {
                TimeZoneWrapper tzw = new TimeZoneWrapper(timeZone);
                String timeZoneText = tzw.getDisplayName(tzw.inDaylightTime(time.toMillis(false)), TimeZone.SHORT);
                return dateTimeText + " " + timeZoneText;
            }
        }

    }
}
