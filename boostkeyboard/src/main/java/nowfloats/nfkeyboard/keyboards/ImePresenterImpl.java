package nowfloats.nfkeyboard.keyboards;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.text.InputType;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import hani.momanii.supernova_emoji_library.emoji.Emojicon;
import nowfloats.nfkeyboard.R;
import nowfloats.nfkeyboard.database.DatabaseTable;
import nowfloats.nfkeyboard.interface_contracts.CandidateToPresenterInterface;
import nowfloats.nfkeyboard.interface_contracts.CandidateViewItemClickListener;
import nowfloats.nfkeyboard.interface_contracts.ImeToPresenterInterface;
import nowfloats.nfkeyboard.interface_contracts.ItemClickListener;
import nowfloats.nfkeyboard.interface_contracts.PresenterToImeInterface;
import nowfloats.nfkeyboard.interface_contracts.UrlToBitmapInterface;
import nowfloats.nfkeyboard.models.AllSuggestionModel;
import nowfloats.nfkeyboard.models.KeywordModel;
import nowfloats.nfkeyboard.util.KeyboardUtils;
import nowfloats.nfkeyboard.util.MethodUtils;
import nowfloats.nfkeyboard.util.MixPanelUtils;
import nowfloats.nfkeyboard.util.SharedPrefUtil;
import timber.log.Timber;

/**
 * Created by Admin on 26-02-2018.
 */

public class ImePresenterImpl implements ItemClickListener,
        ImeToPresenterInterface,
        CandidateToPresenterInterface,
        CandidateViewItemClickListener,
        UrlToBitmapInterface {
    private final DatabaseTable mDatabaseTable;
    private final SpellCorrector corrector;
    private CandidateViewBaseImpl mCandidateView;
    private KeyboardViewBaseImpl mKeyboardView;
    private ManageKeyboardView manageKeyboardView;
    private static final String UTM_SOURCE = "utm_source", UTM_MEDIUM = "utm_medium";
    public final static int KEY_EMOJI = -2005, KEY_IME_OPTION = -2006, KEY_SPACE = -2007, KEY_NUMBER = -2000,
            KEY_SYM = -2001, KEY_SYM_SHIFT = -2002, KEY_QWRTY = -2003, KEY_LANGUAGE_CHANGE = -2004;
    private Context mContext;
    private String packageName = "";
    private KeyboardUtils.CandidateType currentCandidateType = KeyboardUtils.CandidateType.TEXT_LIST;
    private KeyboardUtils.KeyboardType mKeyboardTypeCurrent = KeyboardUtils.KeyboardType.QWERTY_LETTERS;
    private KeyboardBaseImpl mCurrentKeyboard;
    private boolean caps;
    private int imeOptionId;
    private InputMethodManager mInputMethodManager;
    private PresenterToImeInterface imeListener;
    private AudioManager mAudioManager;
    private TabType mTabType = TabType.NO_TAB;
    private ShiftType mShiftType = ShiftType.CAPITAL;
    private int mPrimaryCode;
    private String text;
    private ArrayList<KeywordModel> mSuggestions;
    private static boolean isSelectedKeyboardItem = false;

    @Override
    public TabType getTabType() {
        return mTabType;
    }

    @Override
    public void onSpeechResult(String speech) {
        if (!TextUtils.isEmpty(speech)) {
            if (imeListener.getImeCurrentInputConnection() != null)
                imeListener.getImeCurrentInputConnection().commitText(speech, 1);
            MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SPEECH_RESULT, null);
        }
        if (mTabType != TabType.KEYBOARD) {
            mTabType = TabType.NO_TAB;
            manageKeyboardView.showKeyboardLayout();
            addCandidateTypeView(currentCandidateType, mTabType);
        }
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        if (imeListener.getImeCurrentInputConnection() != null)
            imeListener.getImeCurrentInputConnection().commitText(emojicon.getEmoji(), 1);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        sendKeyEvent(KeyEvent.KEYCODE_DEL);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void onResourcesReady(Bitmap bitmap, String text, String imageId) {
        Uri uri = MethodUtils.getImageUri(mContext, bitmap, TextUtils.isEmpty(imageId) ?
                UUID.randomUUID().toString() : imageId);
        if (uri == null) {
            if (imeListener.getImeCurrentInputConnection() != null)
                imeListener.getImeCurrentInputConnection().commitText(text, 1);
        } else {
            doCommitContent(text, "image/png", uri);
        }
    }

    @Override
    public void onItemClick(KeywordModel word) {
        isSelectedKeyboardItem = true;
        if (word.getType().equalsIgnoreCase(KeywordModel.NEW_WORD)) {
            mDatabaseTable.saveWordToDatabase(word.getWord());
        }
        ExtractedText et = imeListener.getImeCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0);
        int selectionStart = et.selectionStart;
        CharSequence inputSequence = imeListener.getImeCurrentInputConnection().getTextBeforeCursor(1000, 0);
        int index = inputSequence.toString().lastIndexOf(" ");
        String oldText = inputSequence.toString().substring(index > 0 ? index : 0, selectionStart);
        imeListener.getImeCurrentInputConnection().deleteSurroundingText(inputSequence.toString().indexOf(" ") != -1 ?
                oldText.length() - 1 : oldText.length(), 0);
        imeListener.getImeCurrentInputConnection().finishComposingText();
        imeListener.getImeCurrentInputConnection().commitText(word.getWord(), 1);
        imeListener.getImeCurrentInputConnection().finishComposingText();
    }

    @Override
    public void onKeyboardTabClick(View view) {
        if (view.getId() == R.id.img_nowfloats) {
            if (mTabType != TabType.KEYBOARD) {
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_ICON_CLICKED, null);
                // mTabType = TabType.KEYBOARD;
                addCandidateTypeView(KeyboardUtils.CandidateType.BOOST_SHARE1, TabType.NO_TAB);
                manageKeyboardView.showKeyboardLayout();
            }

        } else if (view.getId() == R.id.tv_updates) {
            if (mTabType != TabType.UPDATES) {
                mTabType = TabType.UPDATES;
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SHOW_UPDATES, null);
                manageKeyboardView.showShareLayout(mTabType);
            }
        } else if (view.getId() == R.id.tv_products) {
            if (mTabType != TabType.PRODUCTS) {
                mTabType = TabType.PRODUCTS;
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SHOW_PRODUCT, null);
                manageKeyboardView.showShareLayout(mTabType);
            }
        } else if (view.getId() == R.id.img_settings) {
            if (mTabType != TabType.SETTINGS) {
                mTabType = TabType.SETTINGS;
                manageKeyboardView.showSpeechInput();
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_VOICE_INPUT, null);
            }

        } else if (view.getId() == R.id.img_back) {
            mTabType = TabType.KEYBOARD;
            setCurrentKeyboard();
        }

    }

    public enum TabType {
        PRODUCTS, UPDATES, KEYBOARD, SETTINGS, BACK, NO_TAB;
    }

    private enum ShiftType {
        LOCKED, CAPITAL, NORMAL;
    }

    ImePresenterImpl(Context context, PresenterToImeInterface imeListener) {
        mDatabaseTable = new DatabaseTable(context);
        corrector = new SpellCorrector(context);
        mCandidateView = new CandidateViewBaseImpl(context);
        MixPanelUtils.getInstance().setMixPanel(context);
        mCandidateView.setcandidateItemClickListener(this);
        mContext = context;
        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        this.imeListener = imeListener;
    }

    @Override
    public View onCreateInputView() {
        manageKeyboardView = (ManageKeyboardView) LayoutInflater.from(mContext).inflate(R.layout.keyboard_view, null);
        manageKeyboardView.setPresenterListener(this);
        mKeyboardView = manageKeyboardView.getKeyboard();
        setCurrentKeyboard(mKeyboardTypeCurrent);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardListener());
        return manageKeyboardView;
    }

    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        MixPanelUtils.getInstance().createUser(SharedPrefUtil.fromBoostPref().getsBoostPref(mContext).getFpTag());
        packageName = attribute.packageName;
        mShiftType = ShiftType.CAPITAL;
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
            case InputType.TYPE_CLASS_PHONE:
                setKeyboardType(KeyboardUtils.KeyboardType.NUMBERS);
                currentCandidateType = KeyboardUtils.CandidateType.NULL;
                break;
            case InputType.TYPE_CLASS_TEXT:
                final int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
                switch (variation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        currentCandidateType = KeyboardUtils.CandidateType.NULL;
                        mShiftType = ShiftType.NORMAL;
                        setKeyboardType(KeyboardUtils.KeyboardType.EMAIL_ADDRESS);
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        mShiftType = ShiftType.NORMAL;
                        currentCandidateType = KeyboardUtils.CandidateType.NULL;
                        setKeyboardType(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
                        break;
                    default:
                        //currentCandidateType = KeyboardUtils.CandidateType.BOOST_SHARE;
                        currentCandidateType = KeyboardUtils.CandidateType.TEXT_LIST;
                        setKeyboardType(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
                        showWordSuggestions(imeListener.getImeCurrentInputConnection());
                        break;

                }

                break;
            default:
                //currentCandidateType = KeyboardUtils.CandidateType.BOOST_SHARE;
                currentCandidateType = KeyboardUtils.CandidateType.TEXT_LIST;
                setKeyboardType(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
        }
        imeOptionId = attribute.imeOptions;
        setCurrentKeyboard();
    }

    private void initializeValues() {
        mTabType = TabType.NO_TAB;
        manageKeyboardView.clearResources();
    }

    private void setImeOptions(int options) {
        int keySize = mCurrentKeyboard.getKeys().size();
        Keyboard.Key mEnterKey = null;
        if (keySize > 0) {
            mEnterKey = mCurrentKeyboard.getKeys().get(keySize - 1);
        }
        if (mEnterKey == null) {
            return;
        }
        switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "Go";
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_next_kbd);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_search_kbd);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "send";
                break;
            case EditorInfo.IME_ACTION_DONE:
                mEnterKey.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_check_white_24dp);
                mEnterKey.label = null;
                break;
            default:
                mEnterKey.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_enter_arrow);
                mEnterKey.label = null;
                break;
        }
        //mKeyboardView.invalidateKey(mCurrentKeyboard.getKeys().size()-1);
    }

    public void setKeyboardType(KeyboardUtils.KeyboardType type) {
        mKeyboardTypeCurrent = type;
    }

    public void setCurrentKeyboard() {
        initializeValues();
        setCurrentKeyboard(mKeyboardTypeCurrent);
        addCandidateTypeView(currentCandidateType, mTabType);
    }

    public void setCurrentKeyboard(KeyboardUtils.KeyboardType type) {
        if (mKeyboardTypeCurrent != type) {
            mKeyboardTypeCurrent = type;
        }
        mCurrentKeyboard = mKeyboardView.getKeyboard(type);
        addSvgImages();
        setImeOptions(imeOptionId);
        mKeyboardView.setKeyboard(mCurrentKeyboard);
        mKeyboardView.setShifted(mShiftType != ShiftType.NORMAL);
        manageKeyboardView.showKeyboardLayout();
    }

    private void addSvgImages() {
        for (Keyboard.Key key : mCurrentKeyboard.getKeys()) {
            switch (key.codes[0]) {
                case Keyboard.KEYCODE_DELETE:
                    key.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_backspace_arrow);
                    //inputConnection.deleteSurroundingText(1, 0);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    key.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_arrow_up);
                    break;
                case KEY_EMOJI:
                    key.icon = AppCompatResources.getDrawable(mContext, R.drawable.emoji_happiness);
                    break;
                case KEY_LANGUAGE_CHANGE:
                    key.icon = AppCompatResources.getDrawable(mContext, R.drawable.ic_language_change);
                    break;
            }
        }
    }


    private IBinder getToken() {
        final Dialog dialog = imeListener.getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    public void onDestroy() {
        MixPanelUtils.getInstance().flush();
    }

    private void addCandidateTypeView(KeyboardUtils.CandidateType type, TabType tabType) {
        imeListener.setCandidatesViewShown(mCandidateView.addCandidateTypeView(type, tabType));
    }

    private boolean isCommitContentSupported(
            @Nullable EditorInfo editorInfo, @NonNull String mimeType) {
        if (editorInfo == null) {
            return false;
        }

        final InputConnection ic = imeListener.getImeCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        if (!validatePackageName(editorInfo)) {
            return false;
        }

        final String[] supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        for (String supportedMimeType : supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        final String packageName = editorInfo.packageName;
        if (packageName == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }

        final InputBinding inputBinding = imeListener.getImeCurrentInputBinding();
        if (inputBinding == null) {
            Timber.e("inputBinding should not be null here. "
                    + "You are likely to be hitting b.android.com/225029");
            return false;
        }
        final int packageUid = inputBinding.getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final AppOpsManager appOpsManager =
                    (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            try {
                appOpsManager.checkPackage(packageUid, packageName);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        final PackageManager packageManager = mContext.getPackageManager();
        final String possiblePackageNames[] = packageManager.getPackagesForUid(packageUid);
        for (final String possiblePackageName : possiblePackageNames) {
            if (packageName.equals(possiblePackageName)) {
                return true;
            }
        }
        return false;
    }

    private void doCommitContent(@NonNull String description, @NonNull String mimeType,
                                 @NonNull Uri uri) {

        final EditorInfo editorInfo = imeListener.getImeCurrentEditorInfo();

        if (!validatePackageName(editorInfo)) {
            return;
        }
        final int flag;
        if (Build.VERSION.SDK_INT >= 25) {
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        } else {
            flag = 0;
            try {
                mContext.grantUriPermission(
                        editorInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                Timber.e("grantUriPermission failed packageName=" + editorInfo.packageName
                        + " contentUri=" + uri);
            }
        }

        imeListener.getImeCurrentInputConnection().commitText(description, 1);
        if (isCommitContentSupported(editorInfo, mimeType)) {
            MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_IMAGE_SHARING, null);
            final InputContentInfoCompat inputContentInfoCompat = new InputContentInfoCompat(
                    uri,
                    new ClipDescription(description, new String[]{mimeType}),
                    null /* linkUrl */);
            InputConnectionCompat.commitContent(
                    imeListener.getImeCurrentInputConnection(),
                    imeListener.getImeCurrentEditorInfo(), inputContentInfoCompat,
                    flag, null);
        } else if (mimeType.equalsIgnoreCase("image/png")) {
            Toast.makeText(mContext, "Image not supported", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemClick(final AllSuggestionModel model) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Please grant external storage permission", Toast.LENGTH_SHORT).show();
            //MethodUtils.getPermissions(mContext);
            return;
        }
        if (model == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject object = new JSONObject();
        try {
            object.put("id", model.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uri = null;
        String shareUrl = null;
        try {
            if (!TextUtils.isEmpty(model.getUrl())) {
                uri = Uri.parse(model.getUrl()).buildUpon().appendQueryParameter(UTM_SOURCE, "bk")
                        .appendQueryParameter(UTM_MEDIUM, TextUtils.isEmpty(packageName) ? "share" : packageName).build();
            }
        } catch (Exception e) {

        }
        if (uri != null) {
            shareUrl = uri.toString();
        }

        switch (model.getTypeEnum()) {

            case ImageAndText:

                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_UPDATE_IMAGE_SHARE, object);
                MethodUtils.onGlideBitmapReady(this, model.getText() + ",\nUrl: " + shareUrl, model.getImageUrl(), model.getId());
                break;
            case Product:
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_PRODUCT_SHARE, object);
                MethodUtils.onGlideBitmapReady(this, "Name: " + model.getText() + ",\nUrl: " + shareUrl, model.getImageUrl(), model.getId());
                break;
            case Text:

                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_UPDATE_SHARE, object);
                doCommitContent(model.getText() + ",\nUrl: " + shareUrl, "text/plain", null);
                break;
            default:
                break;
        }
    }


    @Override
    public View getCandidateView() {
        return mCandidateView;
    }

    private void sendKeyEvent(int keyEventCode) {
        if (imeListener.getImeCurrentInputConnection() != null) {
            imeListener.getImeCurrentInputConnection().sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
            imeListener.getImeCurrentInputConnection().sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        }
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_SYM_SHIFT:
                mShiftType = ShiftType.LOCKED;
                mKeyboardView.setShifted(true);
                return true;
        }
        return false;
    }


    public void showWordSuggestions(InputConnection inputConnection) {
        if (mPrimaryCode != 32 && mPrimaryCode != -2007 && currentCandidateType.equals(KeyboardUtils.CandidateType.TEXT_LIST)) {
            CharSequence inputSequence = inputConnection.getTextBeforeCursor(100, 0);
            if (inputSequence != null && inputSequence.length() > 0) {
                text = inputSequence.toString();
                if (inputSequence.toString().indexOf(" ") > 0) {
                    text = inputSequence.toString().substring(inputSequence.toString().lastIndexOf(" "), inputSequence.toString().length());
                }
                // String[] words = inputSequence.toString().split(" ");

                mSuggestions = MethodUtils.fetchWordsFromDatabase(mDatabaseTable, text.trim());
                ArrayList<KeywordModel> corrections = new ArrayList<>();
                if (mSuggestions != null && mSuggestions.isEmpty() && mPrimaryCode != -5) {
                    corrections = corrector.correct(text.trim());
                    mSuggestions = corrections;
                }
                KeywordModel model = new KeywordModel();
                model.setWord(text);
                model.setType(KeywordModel.NEW_WORD);
                mSuggestions.add(0, model);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", mSuggestions);
                mCandidateView.setDataToCandidateType(currentCandidateType, bundle);
            }
        }
    }

    class KeyboardListener extends AbstractKeyboardListener {
        @Override
        public void onPress(int primaryCode) {

            mKeyboardView.setPreviewEnabled(mKeyboardTypeCurrent != KeyboardUtils.KeyboardType.NUMBERS && primaryCode >= 0);
            //mPopUpView.showAtLocation(mCurrentKeyboard.getKeys().get(15),mKeyboardView);
        }

        @Override
        public void onRelease(int primaryCode) {
            //mPopUpView.onRelease();
            mKeyboardView.setPreviewEnabled(false);
        }

        @Override
        public void onKey(int primaryCode, int[] keys) {

            InputConnection inputConnection = imeListener.getImeCurrentInputConnection();
            playClick(primaryCode);
            mPrimaryCode = primaryCode;

            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    //sendKeyEvent(KeyEvent.KEYCODE_DEL);
                    inputConnection.deleteSurroundingText(1, 0);
                    //inputConnection.commitText("", 1);
                    //showWordSuggestions(primaryCode, inputConnection);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    onShiftPressed();
                    break;
                case KEY_IME_OPTION:
                    final EditorInfo editorInfo = imeListener.getImeCurrentEditorInfo();
                    final int imeOptionsActionId = KeyboardUtils.getImeOptionsActionIdFromEditorInfo(editorInfo);
                    if (inputConnection != null && KeyboardUtils.IME_ACTION_CUSTOM_LABEL == imeOptionsActionId) {
                        // Either we have an actionLabel and we should performEditorAction with
                        // actionId regardless of its value.
                        inputConnection.performEditorAction(editorInfo.actionId);
                    } else if (inputConnection != null && EditorInfo.IME_ACTION_NONE != imeOptionsActionId) {
                        // We didn't have an actionLabel, but we had another action to execute.
                        // EditorInfo.IME_ACTION_NONE explicitly means no action. In contrast,
                        // EditorInfo.IME_ACTION_UNSPECIFIED is the default value for an action, so it
                        // means there should be an action and the app didn't bother to set a specific
                        // code for it - presumably it only handles one. It does not have to be treated
                        // in any specific way: anything that is not IME_ACTION_NONE should be sent to
                        // performEditorAction.
                        inputConnection.performEditorAction(imeOptionsActionId);
                    } else {
                        sendKeyEvent(KeyEvent.KEYCODE_ENTER);
                    }

                    break;
                case KEY_NUMBER:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.NUMBERS);
                    break;
                case KEY_EMOJI:
                    mTabType = TabType.NO_TAB;
                    addCandidateTypeView(KeyboardUtils.CandidateType.BOOST_SHARE, TabType.NO_TAB);
                    manageKeyboardView.showEmojiLayout();
                    break;
                case KEY_QWRTY:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
                    break;
                case KEY_SYM:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.SYMBOLS);
                    break;
                case KEY_SYM_SHIFT:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.SYMBOLS_SHIFT);
                    break;
                case KEY_LANGUAGE_CHANGE:
                    showLanguageMethods();
                    break;
                case KEY_SPACE:
                    primaryCode = 32;
                    if (!isSelectedKeyboardItem && mSuggestions != null && !mSuggestions.isEmpty() && mSuggestions.size() > 1 && !text.equalsIgnoreCase(mSuggestions.get(1).getWord())) {
                        ExtractedText et = imeListener.getImeCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0);
                        int selectionStart = et.selectionEnd;
                        CharSequence inputSequence = imeListener.getImeCurrentInputConnection().getTextBeforeCursor(1000, 0);
                        int index = inputSequence.toString().trim().lastIndexOf(" ");
                        String oldText = inputSequence.toString().substring(index > 0 ? index : 0, selectionStart);
                        imeListener.getImeCurrentInputConnection().deleteSurroundingText(inputSequence.toString().trim().indexOf(" ") != -1 ?
                                oldText.length() - 1 : oldText.length(), 0);
                        imeListener.getImeCurrentInputConnection().finishComposingText();
                        imeListener.getImeCurrentInputConnection().commitText(mSuggestions.get(1).getWord(), 1);
                        imeListener.getImeCurrentInputConnection().finishComposingText();
                        mSuggestions.clear();
                        mSuggestions = MethodUtils.fetchWordsFromDatabase(mDatabaseTable, " ");
                        ArrayList<KeywordModel> corrections = new ArrayList<>();
                        if (mSuggestions != null && mSuggestions.isEmpty() && mPrimaryCode != -5) {
                            corrections = corrector.correct(" ");
                            mSuggestions = corrections;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("data", mSuggestions);
                        mCandidateView.setDataToCandidateType(currentCandidateType, bundle);
                    }
                default:
                    isSelectedKeyboardItem = false;
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && mShiftType != ShiftType.NORMAL) {
                        code = Character.toUpperCase(code);
                    }
                    if (inputConnection != null) {
                        inputConnection.commitText(String.valueOf(code), 1);
                    }
                    //showWordSuggestions(primaryCode, inputConnection);

            }
            if (primaryCode != Keyboard.KEYCODE_SHIFT && mShiftType == ShiftType.CAPITAL) {
                mShiftType = ShiftType.NORMAL;
                mKeyboardView.setShifted(false);
            }
        }

        protected void playClick(int keyCode) {

            mKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            if (mAudioManager == null) {
                return;
            }
            switch (keyCode) {
                case KEY_SPACE:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                    break;
                case Keyboard.KEYCODE_DONE:
                case 10:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                    break;
                default:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
            }
        }

        private void showLanguageMethods() {
            boolean isLanguageVisible = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (mInputMethodManager != null) {
                    // if true show keyboard language change button by calling  switchToNextInputMethod()
                    isLanguageVisible = mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
                }
            }
            if (isLanguageVisible) {
                boolean isLastIMESwitched = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    isLastIMESwitched = mInputMethodManager.switchToNextInputMethod(getToken(), false);
                }
                if (!isLastIMESwitched) {
                    mInputMethodManager.switchToLastInputMethod(getToken());
                }
            } else {
                Toast.makeText(mContext, "Unable to show other language keyboard", Toast.LENGTH_SHORT).show();
            }
        }

        private void onShiftPressed() {
            mShiftType = mShiftType == ShiftType.NORMAL ? ShiftType.CAPITAL : ShiftType.NORMAL;
            caps = !caps;
            mKeyboardView.setShifted(mShiftType != ShiftType.NORMAL);
            mKeyboardView.invalidateAllKeys();
        }

        @Override
        public void onText(CharSequence charSequence) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }

    }
}
