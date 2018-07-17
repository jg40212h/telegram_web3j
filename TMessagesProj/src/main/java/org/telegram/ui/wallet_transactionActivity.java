package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class wallet_transactionActivity extends BaseFragment {

    private EditTextBoldCursor AccountField;
    private EditTextBoldCursor how_much_Field;
    private EditTextBoldCursor password_Field;
    private View headerLabelView;
    private View doneButton;

    private String account;
    private String money;
    private String password;
    private HandlerThread mThread;
    private Handler send_transaction;
    private Web3j web3j;
    private Credentials credentials = null;

    private String path = arguments.getString("wallet_path");
    private String web3j_url = arguments.getString("web3j_url");
    private String scan_Address = arguments.getString("scan_Address");

    /*private TextView mTextMessage;
    private Activity theactivity;
    TransactionReceipt transactionReceipt;
    String path;
    String myAddress;
    ImageView QRcode;
    Credentials credentials = null;
    EditText text_account;
    TextView textmybalance;
    TextView textmyhash;
    EditText text_money;
    EditText text_password;
    TextView hash;*/

    private final static int done_button = 1;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("Wallet_Transaction", R.string.WalletTransaction));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    if ( AccountField.getText().length() != 0 && how_much_Field.getText().length() != 0 && password_Field.getText().length() != 0) {
                        //saveName();
                        transaction();
                        finishFragment();
                    }
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        /*TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user == null) {
            user = UserConfig.getCurrentUser();
        }*/

        LinearLayout linearLayout = new LinearLayout(context);
        fragmentView = linearLayout;
        fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
        fragmentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        AccountField = new EditTextBoldCursor(context);
        AccountField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        AccountField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        AccountField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        AccountField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        AccountField.setMaxLines(1);
        AccountField.setLines(1);
        AccountField.setSingleLine(true);
        AccountField.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        AccountField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        AccountField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        AccountField.setHint(LocaleController.getString("Who you want to send to", R.string.Account_transaction));
        if(!scan_Address.equals("error") && scan_Address.length() != 0 ) {
            AccountField.setText(scan_Address);
        }
        //AccountField.setHint(path);
        AccountField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        AccountField.setCursorSize(AndroidUtilities.dp(20));
        AccountField.setCursorWidth(1.5f);
        linearLayout.addView(AccountField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 24, 24, 24, 0));
        AccountField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    how_much_Field.requestFocus();
                    how_much_Field.setSelection(how_much_Field.length());
                    return true;
                }
                return false;
            }
        });

        how_much_Field = new EditTextBoldCursor(context);
        how_much_Field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        how_much_Field.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        how_much_Field.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        how_much_Field.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        how_much_Field.setMaxLines(1);
        how_much_Field.setLines(1);
        how_much_Field.setSingleLine(true);
        how_much_Field.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        how_much_Field.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        how_much_Field.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        how_much_Field.setHint(LocaleController.getString("how much money to send", R.string.how_much_transaction));
        //how_much_Field.setHint(web3j_url);
        how_much_Field.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        how_much_Field.setCursorSize(AndroidUtilities.dp(20));
        how_much_Field.setCursorWidth(1.5f);
        linearLayout.addView(how_much_Field, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 24, 16, 24, 0));
        /*how_much_Field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    doneButton.performClick();
                    return true;
                }
                return false;
            }
        });*/
        how_much_Field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    password_Field.requestFocus();
                    password_Field.setSelection(password_Field.length());
                    return true;
                }
                return false;
            }
        });

        password_Field = new EditTextBoldCursor(context);
        password_Field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        password_Field.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        password_Field.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        password_Field.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        password_Field.setMaxLines(1);
        password_Field.setLines(1);
        password_Field.setSingleLine(true);
        password_Field.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        password_Field.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        password_Field.setImeOptions(EditorInfo.IME_ACTION_DONE);
        password_Field.setHint(LocaleController.getString("your password", R.string.password_transaction));
        password_Field.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        password_Field.setCursorSize(AndroidUtilities.dp(20));
        password_Field.setCursorWidth(1.5f);
        linearLayout.addView(password_Field, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, 24, 16, 24, 0));
        password_Field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    doneButton.performClick();
                    return true;
                }
                return false;
            }
        });


        /*if (user != null) {
            firstNameField.setText(user.first_name);
            firstNameField.setSelection(firstNameField.length());
            lastNameField.setText(user.last_name);
        }*/

        return fragmentView;
    }

    /*@Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        boolean animations = preferences.getBoolean("view_animations", true);
        if (!animations) {
            firstNameField.requestFocus();
            AndroidUtilities.showKeyboard(firstNameField);
        }
    }*/
    //web3j
    public wallet_transactionActivity(Bundle args) {
        super(args);
    }
    private void transaction(){
        account = AccountField.getText().toString();
        money = how_much_Field.getText().toString();
        password = password_Field.getText().toString();
        mThread = new HandlerThread("send");
        mThread.start();
        send_transaction=new Handler(mThread.getLooper());
        web3j = Web3jFactory.build(new HttpService(web3j_url));
        try {
            credentials = WalletUtils.loadCredentials(password, path);
            send_transaction.post(r1);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getParentActivity(), "Wrong password!!", Toast.LENGTH_SHORT).show();
        } catch (CipherException e) {
            e.printStackTrace();
            Toast.makeText(getParentActivity(), "Wrong password!!", Toast.LENGTH_SHORT).show();
        }
    }
    private Runnable r1=new Runnable () {

        @Override
        public void run() {
            try {
                TransactionReceipt transactionReceipt;
                transactionReceipt = Transfer.sendFunds(
                        web3j, credentials, account,
                        BigDecimal.valueOf(1.0), Convert.Unit.ETHER)
                        .send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    //web3j
    /*
    private void saveName() {
        TLRPC.User currentUser = UserConfig.getCurrentUser();
        if (currentUser == null || lastNameField.getText() == null || firstNameField.getText() == null) {
            return;
        }
        String newFirst = firstNameField.getText().toString();
        String newLast = lastNameField.getText().toString();
        if (currentUser.first_name != null && currentUser.first_name.equals(newFirst) && currentUser.last_name != null && currentUser.last_name.equals(newLast)) {
            return;
        }
        TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        req.flags = 3;
        currentUser.first_name = req.first_name = newFirst;
        currentUser.last_name = req.last_name = newLast;
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null) {
            user.first_name = req.first_name;
            user.last_name = req.last_name;
        }
        UserConfig.saveConfig(true);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {

            }
        });
    }*/

    /*@Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (firstNameField != null) {
                        firstNameField.requestFocus();
                        AndroidUtilities.showKeyboard(firstNameField);
                    }
                }
            }, 100);
        }
    }*/

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(AccountField, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(AccountField, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(AccountField, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField),
                new ThemeDescription(AccountField, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated),
                new ThemeDescription(how_much_Field, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(how_much_Field, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(how_much_Field, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField),
                new ThemeDescription(how_much_Field, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated),

                new ThemeDescription(password_Field, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(password_Field, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText),
                new ThemeDescription(password_Field, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField),
                new ThemeDescription(password_Field, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated),

        };
    }
}
