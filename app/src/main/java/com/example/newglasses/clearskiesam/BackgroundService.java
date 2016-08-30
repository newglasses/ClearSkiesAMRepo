/***
 Copyright (c) 2011 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Code tutorial: https://www.sufficientlysecure.org/2012/05/24/service-daily.html
 Source code: https://github.com/commonsguy/cwac-wakeful

 */

package com.example.newglasses.clearskiesam;

import android.content.Intent;

/**
 * Created by newglasses on 09/08/2016.
 * When Broacast is received, the Clear Skies background work begins
 */
public class BackgroundService extends WakefulIntentService {

    public BackgroundService() {
        super("BackgroundService");
    }

    // Asynchronous background operations of service, with wakelock
    @Override
    public void doWakefulWork(Intent intent) {
        // Starts the ClearSkiesService
        Intent i = new Intent(BackgroundService.this, ClearSkiesService.class);
        startService(i);
    }
}
