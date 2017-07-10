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
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.resource.ResourceLocater;
import au.com.codeka.carrot.resource.ResourceName;


/**
 * {@link ResourceLocater} for raw Android resources, uses the id as string.
 *
 * @author Gabor Keszthelyi
 */
public final class RawIdResourceLocator implements ResourceLocater
{
    private final Context mAppContext;


    public RawIdResourceLocator(Context context)
    {
        mAppContext = context.getApplicationContext();
    }


    @Override
    public ResourceName findResource(@Nullable ResourceName parent, String name) throws CarrotException
    {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public ResourceName findResource(String name) throws CarrotException
    {
        return new RawResourceName(name);
    }


    @Override
    public long getModifiedTime(ResourceName resourceName) throws CarrotException
    {
        return 0;
    }


    @Override
    public Reader getReader(ResourceName resourceName) throws CarrotException
    {
        InputStream inputStream = mAppContext.getResources().openRawResource(Integer.valueOf(resourceName.getName()));
        return new InputStreamReader(inputStream);
    }


    private final class RawResourceName extends ResourceName
    {

        RawResourceName(String name)
        {
            super(null, name);
        }


        @Nullable
        @Override
        public ResourceName getParent()
        {
            return null;
        }
    }
}
