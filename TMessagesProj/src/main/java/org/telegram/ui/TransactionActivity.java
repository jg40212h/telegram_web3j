/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.view.View.GONE;
import static android.view.View.combineMeasuredStates;

public class TransactionActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AnimatorSet writeButtonAnimation;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private View extraHeightView;
    private View shadowView;
    private AvatarDrawable avatarDrawable;

    private int extraHeight;

    private int overscrollRow;
    private int emptyRow;
    private int numberSectionRow;
    private int numberRow;
    private int usernameRow;
    private int bioRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;
    private int enableAnimationsRow;
    private int notificationRow;
    private int backgroundRow;
    private int themeRow;
    private int languageRow;
    private int privacyRow;
    private int dataRow;

    //private int saveToGalleryRow;
    private int TransactionRow;

    private int messagesSectionRow;
    private int messagesSectionRow2;
    private int customTabsRow;
    private int directShareRow;
    private int textSizeRow;
    private int stickersRow;
    private int emojiRow;
    private int raiseToSpeakRow;

    //private int sendByEnterRow;
    private int ScanQRCodeRow;

    private int supportSectionRow;
    private int supportSectionRow2;
    private int askQuestionRow;
    private int telegramFaqRow;
    private int privacyPolicyRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int switchBackendButtonRow;
    private int dumpCallStatsRow;
    private int versionRow;
    private int contactsSectionRow;
    private int contactsReimportRow;
    private int contactsSortRow;

    //private int autoplayGifsRow;
    private int CreatQRCodeRow;

    private int rowCount;

    private final static int edit_name = 1;
    private final static int logout = 2;

    //insert_1

    private TextView mTextMessage;
    public Web3j web3j;
    Bundle bundle;
    boolean check = false;
    boolean gogo = false;

    String password;
    String path;
    String web3j_url = "http://140.114.71.5:7654";
    //String web3j_url = "http://140.114.186.53:7654";
    String myAddress="";

    String scan_Address="error";

    /*setContentView(R.layout.activity_main);
    pass = findViewById(R.id.password_main);
    comfirm = findViewById(R.id.comfirm);
    mTextMessage = findViewById(R.id.message);
    error = findViewById(R.id.error);
    error.setText("");
    check = false;
    gogo = false;
    String filePath = Environment.getExternalStorageDirectory().toString() + "/Web3J_test";
    File mFile = new File(filePath);
        if(mFile.exists()){
        comfirm.setVisibility(GONE);
    }*/


    public void open_transaction(){
        web3j = Web3jFactory.build(new HttpService(web3j_url));
        try {
            //Web3ClientVersion web3ClientVersion = null;
            //web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
            //String clientVersion = web3ClientVersion.getWeb3ClientVersion();
            int REQUEST_EXTERNAL_STORAGE = 1;
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            int permission = ActivityCompat.checkSelfPermission(getParentActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        getParentActivity(),
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }


            String filePath = Environment.getExternalStorageDirectory().toString() + "/Telegram" + "/Web3J_test";
            File mFile = new File(filePath);
            String fileName;
            //String password = pass.getText().toString();
            //若沒有檔案儲存路徑時則建立此檔案路徑
            if(!mFile.exists())
            {
                //String com = comfirm.getText().toString();
                mFile.mkdirs();
                fileName = WalletUtils.generateNewWalletFile(password,new File(filePath),false);
                FileWriter mFileWriter = new FileWriter( filePath + "/filename.txt" );
                mFileWriter.write(filePath+"/"+fileName);
                mFileWriter.close();

                path = filePath + "/" +fileName;
                Credentials credentials;
                credentials = WalletUtils.loadCredentials(password, path);
                myAddress = credentials.getAddress();
                FileWriter mPassWriter = new FileWriter( filePath + "/address.txt");
                mPassWriter.write(myAddress);
                mPassWriter.close();

                gogo = true;
            }
            check=true;

            //if(check){
            //String filePath = Environment.getExternalStorageDirectory().toString() + "/Telegram" + "/Web3J_test";
            /*String mTextLine;
            FileReader mFileReader = new FileReader(filePath+ "/filename.txt");
            BufferedReader mBufferedReader = new BufferedReader(mFileReader);
            mTextLine = mBufferedReader.readLine();
            mFileReader.close();
            bundle = new Bundle();
            bundle.putString("path",mTextLine);*/
            //}

        }  catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

    }

    public void set_transaction(){
        final View password_item = LayoutInflater.from(getParentActivity()).inflate(R.layout.creat_password, null);
        final EditText pass = (EditText) password_item.findViewById(R.id.set_password);
        final EditText confirm = (EditText) password_item.findViewById(R.id.confirm_password);
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder password_builder = new AlertDialog.Builder(getParentActivity());
        password_builder.setTitle(LocaleController.getString("Transaction", R.string.create_password));
        //password_builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureClearHistory));
        /*final EditText pass = new EditText(getParentActivity());
        final EditText confirm = new EditText(getParentActivity());
        LinearLayout pass_layout = new LinearLayout(getParentActivity());
        pass_layout.addView(pass);
        pass_layout.addView(confirm);
        password_builder.setView(pass_layout);*/
        password_builder.setView(password_item);
        password_builder.setPositiveButton(LocaleController.getString("ChatTransactionOK", R.string.ChatTransaction_OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                password = pass.getText().toString();
                String confirm_pass = confirm.getText().toString();
                if(!password.equals(confirm_pass)){
                    Toast.makeText(getParentActivity(), "The confirm password is different from password!!", Toast.LENGTH_SHORT).show();
                }
                else if(password.isEmpty() || confirm_pass.isEmpty()){
                    Toast.makeText(getParentActivity(), "password can't be empty!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    open_transaction();
                    Toast.makeText(getParentActivity(), "Create password", Toast.LENGTH_SHORT).show();
                    finishFragment();
                }
                //pass = getParentActivity().findViewById(R.id.creat_password);
                //confirm = getParentActivity().findViewById(R.id.confirm_password);

                /*String filePath = Environment.getExternalStorageDirectory().toString() + "/Web3J_test";
                File mFile = new File(filePath);
                if(mFile.exists()){
                    confirm.setVisibility(GONE);
                }*/
            }
        });
        //transaction_builder.setPositiveButton(LocaleController.getString("ChatTransactionOK", R.string.ChatTransaction_OK), null);
        password_builder.setNegativeButton(LocaleController.getString("ChatTransactionNO", R.string.ChatTransaction_NO), null);
        showDialog(password_builder.create());
    }
    public void QR_code_draw(){
        final View QR_code_item = LayoutInflater.from(getParentActivity()).inflate(R.layout.qr_code_draw, null);
        final ImageView QR_code = (ImageView) QR_code_item.findViewById(R.id.QR_code_draw);
        final TextView account_view = (TextView) QR_code_item.findViewById(R.id.your_account);
        if (getParentActivity() == null) {
            return;
        }

        BarcodeEncoder encoder = new BarcodeEncoder();
        try {
            Bitmap bit = encoder.encodeBitmap(myAddress, BarcodeFormat.QR_CODE,
                    750, 750);
            QR_code.setImageBitmap(bit);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        account_view.setText(myAddress);
        AlertDialog.Builder QR_code_builder = new AlertDialog.Builder(getParentActivity());
        QR_code_builder.setTitle(LocaleController.getString("YOUR ACCOUNT", R.string.qr_code_draw_title));
        QR_code_builder.setView(QR_code_item);
        QR_code_builder.setNegativeButton(LocaleController.getString("Return", R.string.qr_code_return), null);
        showDialog(QR_code_builder.create());
    }

    /*public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(scanningResult!=null){
            Toast.makeText(getParentActivity(),"success",Toast.LENGTH_SHORT).show();
            scan_Address = scanningResult.getContents();
            Bundle args = new Bundle();
            args.putString("wallet_path", path);
            args.putString("wallet_password", password);
            args.putString("web3j_url",web3j_url);
            args.putString("scan_Address",scan_Address);
            presentFragment(new wallet_transactionActivity(args));
        }else{
            Toast.makeText(getParentActivity(),"nothing",Toast.LENGTH_SHORT).show();
        }
    }*/

    //insert_1

    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
            if (fileLocation == null) {
                return null;
            }
            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                TLRPC.FileLocation photoBig = user.photo.photo_big;
                if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                    int coords[] = new int[2];
                    avatarImage.getLocationInWindow(coords);
                    PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                    object.viewX = coords[0];
                    object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                    object.parentView = avatarImage;
                    object.imageReceiver = avatarImage.getImageReceiver();
                    object.dialogId = UserConfig.getClientUserId();
                    object.thumb = object.imageReceiver.getBitmap();
                    object.size = -1;
                    object.radius = avatarImage.getImageReceiver().getRoundRadius();
                    object.scale = avatarImage.getScaleX();
                    return object;
                }
            }
            return null;
        }

        @Override
        public void willHidePhotoViewer() {
            avatarImage.getImageReceiver().setVisible(true, true);
        }
    };

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer, @NonNull MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e(e);
            }
            return false;
        }
    }

    //insert_2
    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.featuredStickersDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.userInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);

        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;
        messagesSectionRow = rowCount++;
        messagesSectionRow2 = rowCount++;
        ScanQRCodeRow = rowCount++;
        CreatQRCodeRow = rowCount++;
        TransactionRow = rowCount++;

        StickersQuery.checkFeaturedStickers();
        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid, true);

        return true;
    }
    //insert_2

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.featuredStickersDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.userInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        avatarUpdater.clear();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_avatar_actionBarSelectorBlue), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_avatar_actionBarIconBlue), false);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) {
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child == listView) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (parentLayout != null) {
                        int actionBarHeight = 0;
                        int childCount = getChildCount();
                        for (int a = 0; a < childCount; a++) {
                            View view = getChildAt(a);
                            if (view == child) {
                                continue;
                            }
                            if (view instanceof ActionBar && view.getVisibility() == VISIBLE) {
                                if (((ActionBar) view).getCastShadows()) {
                                    actionBarHeight = view.getMeasuredHeight();
                                }
                                break;
                            }
                        }
                        parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                    }
                    return result;
                } else {
                    return super.drawChild(canvas, child, drawingTime);
                }
            }
        };

        //insert_3
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                //String filePath = Environment.getExternalStorageDirectory().toString() + "/Web3J_test";
                String filePath = Environment.getExternalStorageDirectory().toString() + "/Telegram" + "/Web3J_test";
                File mFile = new File(filePath);
                if(!mFile.exists()){
                    set_transaction();
                }
                else {
                    open_transaction();
                    if (position == ScanQRCodeRow) {
                        scan_Address = "error";
                        if (getParentActivity() == null) {
                            return;
                        }
                        IntentIntegrator scanIntegrator = new IntentIntegrator(getParentActivity());
                        scanIntegrator.setOrientationLocked(false);
                        scanIntegrator.setBeepEnabled(false);
                        scanIntegrator.setCameraId(0);
                        scanIntegrator.initiateScan();
                    } else if (position == CreatQRCodeRow) {
                        QR_code_draw();
                    } else if (position == TransactionRow) {
                        Bundle args = new Bundle();
                        args.putString("wallet_path", path);
                        args.putString("wallet_password", password);
                        args.putString("web3j_url",web3j_url);
                        args.putString("scan_Address","error");
                        presentFragment(new wallet_transactionActivity(args));
                    }
                }
            }
        });
        //insert_3

        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {

            private int pressCount = 0;

            @Override
            public boolean onItemClick(View view, int position) {
                if (position == versionRow) {
                    pressCount++;
                    if (pressCount >= 2 || BuildVars.DEBUG_PRIVATE_VERSION) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu));
                        CharSequence[] items;
                        if (BuildVars.DEBUG_PRIVATE_VERSION) {
                            items = new CharSequence[]{
                                    LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts),
                                    LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts),
                                    LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts),
                                    LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs),
                                    MediaController.getInstance().canInAppCamera() ? LocaleController.getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) : LocaleController.getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera),
                                    MediaController.getInstance().canRoundCamera16to9() ? "switch camera to 4:3" : "switch camera to 16:9"
                            };
                        } else {
                            items = new CharSequence[]{
                                    LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts),
                                    LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts),
                                    LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts),
                                    LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs),
                                    MediaController.getInstance().canInAppCamera() ? LocaleController.getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) : LocaleController.getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera)
                            };
                        }
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    ContactsController.getInstance().forceImportContacts();
                                } else if (which == 1) {
                                    ContactsController.getInstance().loadContacts(false, 0);
                                } else if (which == 2) {
                                    ContactsController.getInstance().resetImportedContacts();
                                } else if (which == 3) {
                                    MessagesController.getInstance().forceResetDialogs();
                                } else if (which == 4) {
                                    MediaController.getInstance().toggleInappCamera();
                                } else if (which == 5) {
                                    MediaController.getInstance().toggleRoundCamera16to9();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else {
                        try {
                            Toast.makeText(getParentActivity(), "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        extraHeightView.setPivotY(0);
        extraHeightView.setBackgroundColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarImage.setPivotX(0);
        avatarImage.setPivotY(0);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_profile_title));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setPivotX(0);
        nameTextView.setPivotY(0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(Theme.getColor(Theme.key_avatar_subtitleInProfileBlue));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        writeButton = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_profile_actionBackground), Theme.getColor(Theme.key_profile_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }


        needLayout();

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager.getItemCount() == 0) {
                    return;
                }
                int height = 0;
                View child = recyclerView.getChildAt(0);
                if (child != null) {
                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    private void performAskAQuestion() {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        int uid = preferences.getInt("support_id", 0);
        TLRPC.User supportUser = null;
        if (uid != 0) {
            supportUser = MessagesController.getInstance().getUser(uid);
            if (supportUser == null) {
                String userString = preferences.getString("support_user", null);
                if (userString != null) {
                    try {
                        byte[] datacentersBytes = Base64.decode(userString, Base64.DEFAULT);
                        if (datacentersBytes != null) {
                            SerializedData data = new SerializedData(datacentersBytes);
                            supportUser = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                            if (supportUser != null && supportUser.id == 333000) {
                                supportUser = null;
                            }
                            data.cleanup();
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                        supportUser = null;
                    }
                }
            }
        }
        if (supportUser == null) {
            final AlertDialog progressDialog = new AlertDialog(getParentActivity(), 1);
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            TLRPC.TL_help_getSupport req = new TLRPC.TL_help_getSupport();
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {
                    if (error == null) {

                        final TLRPC.TL_help_support res = (TLRPC.TL_help_support) response;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("support_id", res.user.id);
                                SerializedData data = new SerializedData();
                                res.user.serializeToStream(data);
                                editor.putString("support_user", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                                editor.commit();
                                data.cleanup();
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                                ArrayList<TLRPC.User> users = new ArrayList<>();
                                users.add(res.user);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, true, true);
                                MessagesController.getInstance().putUser(res.user, false);
                                Bundle args = new Bundle();
                                args.putInt("user_id", res.user.id);
                                presentFragment(new ChatActivity(args));
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            MessagesController.getInstance().putUser(supportUser, true);
            Bundle args = new Bundle();
            args.putInt("user_id", supportUser.id);
            presentFragment(new ChatActivity(args));
        }
    }

    //insert_4
    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent intent){
        if (resultCode == Activity.RESULT_OK && intent != null) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                Toast.makeText(getParentActivity(), "success", Toast.LENGTH_SHORT).show();
                scan_Address = scanningResult.getContents();
                Bundle args = new Bundle();
                args.putString("wallet_path", path);
                args.putString("wallet_password", password);
                args.putString("web3j_url", web3j_url);
                args.putString("scan_Address", scan_Address);
                presentFragment(new wallet_transactionActivity(args));
            } else {
                //super.onActivityResultFragment(requestCode, resultCode, intent);
                avatarUpdater.onActivityResult(requestCode, resultCode, intent);
                Toast.makeText(getParentActivity(), "nothing", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //insert_4

    /*@Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }*/

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        } else if (id == NotificationCenter.featuredStickersDidLoaded) {
            if (listAdapter != null) {
                listAdapter.notifyItemChanged(stickersRow);
            }
        } else if (id == NotificationCenter.userInfoDidLoaded) {
            Integer uid = (Integer) args[0];
            if (uid == UserConfig.getClientUserId() && listAdapter != null) {
                listAdapter.notifyItemChanged(bioRow);
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                extraHeightView.setTranslationY(newTop);
            }
        }

        if (avatarImage != null) {
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            extraHeightView.setScaleY(diff);
            shadowView.setTranslationY(newTop + extraHeight);


            writeButton.setTranslationY((actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f));

            final boolean setVisible = diff > 0.2f;
            boolean currentVisible = writeButton.getTag() == null;
            if (setVisible != currentVisible) {
                if (setVisible) {
                    writeButton.setTag(null);
                    writeButton.setVisibility(View.VISIBLE);
                } else {
                    writeButton.setTag(0);
                }
                if (writeButtonAnimation != null) {
                    AnimatorSet old = writeButtonAnimation;
                    writeButtonAnimation = null;
                    old.cancel();
                }
                writeButtonAnimation = new AnimatorSet();
                if (setVisible) {
                    writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimator.ofFloat(writeButton, "scaleX", 1.0f),
                            ObjectAnimator.ofFloat(writeButton, "scaleY", 1.0f),
                            ObjectAnimator.ofFloat(writeButton, "alpha", 1.0f)
                    );
                } else {
                    writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimator.ofFloat(writeButton, "scaleX", 0.2f),
                            ObjectAnimator.ofFloat(writeButton, "scaleY", 0.2f),
                            ObjectAnimator.ofFloat(writeButton, "alpha", 0.0f)
                    );
                }
                writeButtonAnimation.setDuration(150);
                writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                            writeButton.setVisibility(setVisible ? View.VISIBLE : View.GONE);
                            writeButtonAnimation = null;
                        }
                    }
                });
                writeButtonAnimation.start();
            }

            avatarImage.setScaleX((42 + 18 * diff) / 42.0f);
            avatarImage.setScaleY((42 + 18 * diff) / 42.0f);
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            avatarImage.setTranslationX(-AndroidUtilities.dp(47) * diff);
            avatarImage.setTranslationY((float) Math.ceil(avatarY));
            nameTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
            nameTextView.setTranslationY((float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            onlineTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
            onlineTextView.setTranslationY((float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float )Math.floor(11 * AndroidUtilities.density) * diff);
            nameTextView.setScaleX(1.0f + 0.12f * diff);
            nameTextView.setScaleY(1.0f + 0.12f * diff);
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }
    //insert_5
    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        avatarDrawable = new AvatarDrawable(user, true);

        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
            String name = UserObject.getUserName(user);
            name += "'s Wallet";
            nameTextView.setText(name);
            //private int moneynumber = 0;
            //
            //String de = "test";
            String balance = "error_01";
            try {
                //String myAddress;
                String filePath_1 = Environment.getExternalStorageDirectory().toString() + "/Telegram" + "/Web3J_test" + "/filename.txt";
                String filePath_2 = Environment.getExternalStorageDirectory().toString() + "/Telegram" + "/Web3J_test" + "/address.txt";
                web3j = Web3jFactory.build(new HttpService(web3j_url));
                File mFile_1 = new File(filePath_1);
                File mFile_2 = new File(filePath_2);
                balance = "papo";
                if(mFile_1.exists() && mFile_2.exists()) {
                    FileReader mFileReader_1 = new FileReader(filePath_1);
                    BufferedReader mBufferedReader_1 = new BufferedReader(mFileReader_1);
                    path = mBufferedReader_1.readLine();
                    mFileReader_1.close();
                    balance = "papo22";
                    FileReader mFileReader_2 = new FileReader(filePath_2);
                    BufferedReader mBufferedReader_2 = new BufferedReader(mFileReader_2);
                    myAddress = mBufferedReader_2.readLine();
                    mFileReader_2.close();
                    //de = password + "||" + path;
                    //bundle = ne w Bundle();
                    //bundle.putString("path", mTextLine);
                    //path = bundle.getString("path");

                    balance = "papo33";

                    EthGetBalance ethGetBalance = web3j
                            .ethGetBalance(myAddress, DefaultBlockParameterName.LATEST)
                            .sendAsync()
                            .get();
                    balance = "papo44";
                    BigInteger wei = ethGetBalance.getBalance();
                    String total = wei.divide(new BigInteger("10000000000000000")).toString();
                    double value = Double.parseDouble(total);
                    balance = "no money??";
                    balance = String.valueOf(value / 100);
                    //Toast.makeText(getParentActivity(), balance, Toast.LENGTH_SHORT).show();
                }
                else{
                    balance = "oh my god";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }  catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //
            String balance_title = LocaleController.getString("yourmoney", R.string.yourmoney);
            String output = balance_title + ": " + balance;
            onlineTextView.setText(output);

            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }
    //insert_5

    private void sendLogs() {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
            File dir = new File(sdCard.getAbsolutePath() + "/logs");
            File[] files = dir.listFiles();

            for (File file : files) {
                if (Build.VERSION.SDK_INT >= 24) {
                    uris.add(FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", file));
                } else {
                    uris.add(Uri.fromFile(file));
                }
            }

            if (uris.isEmpty()) {
                return;
            }
            Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            if (Build.VERSION.SDK_INT >= 24) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, "");
            i.putExtra(Intent.EXTRA_SUBJECT, "last logs");
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            getParentActivity().startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    if (position == overscrollRow) {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(88));
                    } else {
                        ((EmptyCell) holder.itemView).setHeight(AndroidUtilities.dp(16));
                    }
                    break;
                }
                case 2: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == textSizeRow) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        int size = preferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
                        textCell.setTextAndValue(LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                    } else if (position == languageRow) {
                        textCell.setTextAndValue(LocaleController.getString("Language", R.string.Language), LocaleController.getCurrentLanguageName(), true);
                    } else if (position == themeRow) {
                        textCell.setTextAndValue(LocaleController.getString("Theme", R.string.Theme), Theme.getCurrentThemeName(), true);
                    } else if (position == contactsSortRow) {
                        String value;
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        int sort = preferences.getInt("sortContactsBy", 0);
                        if (sort == 0) {
                            value = LocaleController.getString("Default", R.string.Default);
                        } else if (sort == 1) {
                            value = LocaleController.getString("FirstName", R.string.SortFirstName);
                        } else {
                            value = LocaleController.getString("LastName", R.string.SortLastName);
                        }
                        textCell.setTextAndValue(LocaleController.getString("SortBy", R.string.SortBy), value, true);
                    } else if (position == notificationRow) {
                        textCell.setText(LocaleController.getString("NotificationsAndSounds", R.string.NotificationsAndSounds), true);
                    } else if (position == backgroundRow) {
                        textCell.setText(LocaleController.getString("ChatBackground", R.string.ChatBackground), true);
                    } else if (position == sendLogsRow) {
                        textCell.setText("Send Logs", true);
                    } else if (position == clearLogsRow) {
                        textCell.setText("Clear Logs", true);
                    } else if (position == askQuestionRow) {
                        textCell.setText(LocaleController.getString("AskAQuestion", R.string.AskAQuestion), true);
                    } else if (position == privacyRow) {
                        textCell.setText(LocaleController.getString("PrivacySettings", R.string.PrivacySettings), true);
                    } else if (position == dataRow) {
                        textCell.setText(LocaleController.getString("DataSettings", R.string.DataSettings), true);
                    } else if (position == switchBackendButtonRow) {
                        textCell.setText("Switch Backend", true);
                    } else if (position == telegramFaqRow) {
                        textCell.setText(LocaleController.getString("TelegramFAQ", R.string.TelegramFAQ), true);
                    } else if (position == contactsReimportRow) {
                        textCell.setText(LocaleController.getString("ImportContacts", R.string.ImportContacts), true);
                    } else if (position == stickersRow) {
                        int count = StickersQuery.getUnreadStickerSets().size();
                        textCell.setTextAndValue(LocaleController.getString("StickersName", R.string.StickersName), count != 0 ? String.format("%d", count) : "", true);
                    } else if (position == privacyPolicyRow) {
                        textCell.setText(LocaleController.getString("PrivacyPolicy", R.string.PrivacyPolicy), true);
                    } else if (position == emojiRow) {
                        textCell.setText(LocaleController.getString("Emoji", R.string.Emoji), true);
                    } else if (position == ScanQRCodeRow) {
                        textCell.setText(LocaleController.getString("scanqrcode", R.string.scanQRcode), true);
                    } else if (position == CreatQRCodeRow) {
                        textCell.setText(LocaleController.getString("creatqrcode", R.string.creatQRcode), true);
                    } else if (position == TransactionRow) {
                        textCell.setText(LocaleController.getString("transaction", R.string.transaction), true);
                    }
                    break;
                }
                case 3: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    if (position == enableAnimationsRow) {
                        textCell.setTextAndCheck(LocaleController.getString("EnableAnimations", R.string.EnableAnimations), preferences.getBoolean("view_animations", true), false);
                    }  else if (position == raiseToSpeakRow) {
                        textCell.setTextAndCheck(LocaleController.getString("RaiseToSpeak", R.string.RaiseToSpeak), MediaController.getInstance().canRaiseToSpeak(), true);
                    } else if (position == customTabsRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("ChromeCustomTabs", R.string.ChromeCustomTabs), LocaleController.getString("ChromeCustomTabsInfo", R.string.ChromeCustomTabsInfo), MediaController.getInstance().canCustomTabs(), false, true);
                    } else if (position == directShareRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("DirectShare", R.string.DirectShare), LocaleController.getString("DirectShareInfo", R.string.DirectShareInfo), MediaController.getInstance().canDirectShare(), false, true);
                    } else if (position == dumpCallStatsRow) {
                        textCell.setTextAndCheck("Dump detailed call stats", preferences.getBoolean("dbg_dump_call_stats", false), true);
                    }
                    break;
                }
                case 4: {
                    if (position == settingsSectionRow2) {
                        ((HeaderCell) holder.itemView).setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                    } else if (position == supportSectionRow2) {
                        ((HeaderCell) holder.itemView).setText(LocaleController.getString("Support", R.string.Support));
                    } else if (position == messagesSectionRow2) {
                        ((HeaderCell) holder.itemView).setText(LocaleController.getString("Wallet", R.string.Wallet));
                    } else if (position == numberSectionRow) {
                        ((HeaderCell) holder.itemView).setText(LocaleController.getString("Info", R.string.Info));
                    }
                    break;
                }
                case 6: {
                    TextDetailSettingsCell textCell = (TextDetailSettingsCell) holder.itemView;

                    if (position == numberRow) {
                        TLRPC.User user = UserConfig.getCurrentUser();
                        String value;
                        if (user != null && user.phone != null && user.phone.length() != 0) {
                            value = PhoneFormat.getInstance().format("+" + user.phone);
                        } else {
                            value = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
                        }
                        textCell.setTextAndValue(value, LocaleController.getString("Phone", R.string.Phone), true);
                    } else if (position == usernameRow) {
                        TLRPC.User user = UserConfig.getCurrentUser();
                        String value;
                        if (user != null && !TextUtils.isEmpty(user.username)) {
                            value = "@" + user.username;
                        } else {
                            value = LocaleController.getString("UsernameEmpty", R.string.UsernameEmpty);
                        }
                        textCell.setTextAndValue(value, LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == bioRow) {
                        TLRPC.TL_userFull userFull = MessagesController.getInstance().getUserFull(UserConfig.getClientUserId());
                        String value;
                        if (userFull == null) {
                            value = LocaleController.getString("Loading", R.string.Loading);
                        } else if (!TextUtils.isEmpty(userFull.about)) {
                            value = userFull.about;
                        } else {
                            value = LocaleController.getString("UserBioEmpty", R.string.UserBioEmpty);
                        }
                        textCell.setTextWithEmojiAndValue(value, LocaleController.getString("UserBio", R.string.UserBio), false);
                    }
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == textSizeRow || position == enableAnimationsRow || position == notificationRow || position == backgroundRow || position == numberRow ||
                    position == askQuestionRow || position == sendLogsRow || position == ScanQRCodeRow || position == CreatQRCodeRow || position == privacyRow ||
                    position == clearLogsRow || position == languageRow || position == usernameRow || position == bioRow ||
                    position == switchBackendButtonRow || position == telegramFaqRow || position == contactsSortRow || position == contactsReimportRow || position == TransactionRow ||
                    position == stickersRow || position == raiseToSpeakRow || position == privacyPolicyRow || position == customTabsRow || position == directShareRow || position == versionRow ||
                    position == emojiRow || position == dataRow || position == themeRow || position == dumpCallStatsRow;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new EmptyCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 2:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new TextInfoCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 0:
                                abi = "arm";
                                break;
                            case 1:
                            case 3:
                                abi = "arm-v7a";
                                break;
                            case 2:
                            case 4:
                                abi = "x86";
                                break;
                            case 5:
                                abi = "universal";
                                break;
                        }
                        ((TextInfoCell) view).setText(LocaleController.formatString("TelegramVersion", R.string.TelegramVersion, String.format(Locale.US, "v%s (%d) %s", pInfo.versionName, code, abi)));
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                    break;
                case 6:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == emptyRow || position == overscrollRow) {
                return 0;
            }
            if (position == settingsSectionRow || position == supportSectionRow || position == messagesSectionRow || position == contactsSectionRow) {
                return 1;
            } else if (position == enableAnimationsRow || position == raiseToSpeakRow || position == customTabsRow || position == directShareRow || position == dumpCallStatsRow) {
                return 3;
            } else if (position == notificationRow || position == themeRow || position == backgroundRow || position == askQuestionRow || position == sendLogsRow || position == privacyRow || position == clearLogsRow || position == switchBackendButtonRow || position == telegramFaqRow || position == contactsReimportRow || position == textSizeRow || position == languageRow || position == contactsSortRow || position == stickersRow || position == privacyPolicyRow || position == emojiRow || position == dataRow || position == ScanQRCodeRow || position == CreatQRCodeRow || position == TransactionRow) {
                return 2;
            } else if (position == versionRow) {
                return 5;
            } else if (position == numberRow || position == usernameRow || position == bioRow) {
                return 6;
            } else if (position == settingsSectionRow2 || position == messagesSectionRow2 || position == supportSectionRow2 || position == numberSectionRow) {
                return 4;
            } else {
                return 2;
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextInfoCell.class, TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(extraHeightView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(nameTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_profile_title),
                new ThemeDescription(onlineTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_avatar_subtitleInProfileBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumb),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchThumbChecked),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),

                new ThemeDescription(listView, 0, new Class[]{TextInfoCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText5),

                new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(avatarImage, 0, null, null, new Drawable[]{avatarDrawable}, null, Theme.key_avatar_backgroundInProfileBlue),

                new ThemeDescription(writeButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_profile_actionIcon),
                new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_profile_actionBackground),
                new ThemeDescription(writeButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_profile_actionPressedBackground),
        };
    }
}
