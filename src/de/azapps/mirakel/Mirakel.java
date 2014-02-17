/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import de.azapps.mirakel.helper.Helpers;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.helper.export_import.ExportImport;
import de.azapps.mirakel.model.DatabaseHelper;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.file.FileMirakel;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;
import de.azapps.mirakel.model.recurring.Recurring;
import de.azapps.mirakel.model.semantic.Semantic;
import de.azapps.mirakel.model.task.Task;
import de.azapps.mirakel.services.NotificationService;
import de.azapps.mirakelandroid.R;

@SuppressLint("RtlHardcoded")
@ReportsCrashes(
		// This is required for backward compatibility but not used
		formKey = "",
		// optional, displayed as soon as the crash occurs, before collecting data which
		// can take a few seconds
		reportType = org.acra.sender.HttpSender.Type.JSON,
		httpMethod = org.acra.sender.HttpSender.Method.PUT,
		formUri = "http://couchdb.azapps.de/acra-mirakel/_design/acra-storage/_update/report",
		formUriBasicAuthLogin = "mirakel",
		formUriBasicAuthPassword = "Ieshi8Egheic0etaipeeTeibo",
		disableSSLCertValidation = true,
		mode = ReportingInteractionMode.DIALOG,
		resToastText = R.string.crash_toast_text,
		// optional. default is a warning sign
		resDialogText = R.string.crash_dialog_text,
		resDialogIcon = android.R.drawable.ic_dialog_info,
		// optional. default is your application name
		resDialogTitle = R.string.crash_dialog_title,
		// optional. when defined, adds a user text field input with this text resource
		// as a label
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
		resDialogOkToast = R.string.crash_dialog_ok_toast
// optional. displays a Toast message when the user accepts to send a report.

)
public class Mirakel extends Application {
	// Public Constants
	public static class NoSuchListException extends Exception {
		static final long	serialVersionUID	= 1374828057;
	}

	public static class NoSuchTaskException extends Exception {
		static final long	serialVersionUID	= 1374828058;
	}

	public static final String		AUTHORITY_TYP	= "de.azapps.mirakel.provider";
	public static final int			NOTIF_DEFAULT	= 123,
			NOTIF_REMINDER = 124;

	public static final File		exportDir		= new File(
															Environment
																	.getExternalStorageDirectory(),
															"mirakel");
	public static final String		SYNC_FINISHED	= "de.azapps.mirakel.sync_finished";

	// Other stuff

	public static String			APK_NAME;

	// FIXME move this somewhere else?
	public static int				GRAVITY_LEFT, GRAVITY_RIGHT;

	public static boolean			IS_PLAYSTORE;

	public static String			MIRAKEL_DIR;
	private static SQLiteOpenHelper	openHelper;
	private static final String		TAG				= "Mirakel";

	public static String			VERSIONS_NAME;

	public static int				widgets[]		= {};

	public static SQLiteDatabase getReadableDatabase() {
		return openHelper.getReadableDatabase();
	}

	public static SQLiteDatabase getWritableDatabase() {
		return openHelper.getWritableDatabase();
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate() {
		super.onCreate();
		// Init variables
		APK_NAME = getPackageName();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			GRAVITY_LEFT = Gravity.START;
			GRAVITY_RIGHT = Gravity.END;
		} else {
			GRAVITY_LEFT = Gravity.LEFT;
			GRAVITY_RIGHT = Gravity.RIGHT;
		}

		try {
			VERSIONS_NAME = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.wtf(TAG, "App not found");
			VERSIONS_NAME = "";
		}

		// This we have to initialize as early as possible
		MirakelPreferences.init(this);
		IS_PLAYSTORE = getResources().getBoolean(R.bool.is_playstore);
		Locale locale = Helpers.getLocal(this);
		Locale.setDefault(locale);

		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		openHelper = new DatabaseHelper(this);
		Mirakel.getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");

		// Initialize Models

		ListMirakel.init(this);
		Task.init(this);
		SpecialList.init(this);
		FileMirakel.init(this);
		Semantic.init(this);
		Recurring.init(this);
		AccountMirakel.init(this);

		// And now, after the Database initialization!!! We init ACRA
		ACRA.init(this);

		// Stuff we can do in another thread
		final Mirakel that = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				// Notifications
				if (!MirakelPreferences.useNotifications()
						&& startService(new Intent(that,
								NotificationService.class)) != null) {
					stopService(new Intent(Mirakel.this,
							NotificationService.class));
				}
				// Auto Backup?
				Calendar nextBackup = MirakelPreferences.getNextAutoBackup();
				if (nextBackup != null
						&& nextBackup.compareTo(new GregorianCalendar()) < 0) {
					ExportImport.exportDB(that);
					Calendar nextB = new GregorianCalendar();
					nextB.add(Calendar.DATE,
							MirakelPreferences.getAutoBackupIntervall());
					MirakelPreferences.setNextBackup(nextB);
				}
			}
		}).start();

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ListMirakel.close();
		Task.close();
		SpecialList.close();
		FileMirakel.close();
		Semantic.close();
		Recurring.close();
		AccountMirakel.close();
	}
}
