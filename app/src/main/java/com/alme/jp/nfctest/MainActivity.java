package com.alme.jp.nfctest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    ToggleButton tglReadWrite;
    EditText txtTagContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        tglReadWrite =(ToggleButton) findViewById(R.id.tglReadWrite);
        txtTagContent =(EditText) findViewById(R.id.txtTagContent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            Toast.makeText(this,"NfcIntent!", Toast.LENGTH_LONG).show();
            byte[] pload = "test".getBytes();
            NdefRecord ndefRecord = NdefRecord.createExternal("nfctest","externaltype", pload);
//            NdefMessage ndefMsg =  new NdefMessage(new NdefRecord[]{ndefRecord});
//            writeNdefMessage(tag, ndefMsg);

            if(tglReadWrite.isChecked()){
                Toast.makeText(this,"isCheked", Toast.LENGTH_LONG).show();
                Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if(parcelables != null && parcelables.length > 0){
                    readTextFromMessage((NdefMessage)parcelables[0]);
                }
            }else{
                Toast.makeText(this,"not checked", Toast.LENGTH_LONG).show();
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                NdefMessage  ndefMessage = createNdefMessage(txtTagContent.getText()+"");
                writeNdefMessage(tag, ndefMessage);
            }

        }
        Toast.makeText(this,"NfcIntent! 2", Toast.LENGTH_LONG).show();
    }

    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if(ndefRecords != null && ndefRecords.length > 0){

            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);

            txtTagContent.setText(tagContent);
            Toast.makeText(this,tagContent,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "No NDEF messages found!", Toast.LENGTH_LONG).show();
        }
    }

    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try{
            NdefFormatable ndefFormatable =  NdefFormatable.get(tag);
            if(ndefFormatable == null){
                Toast.makeText(this,"Tag is not ndef formatable!", Toast.LENGTH_LONG).show();
            }

            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();
            Toast.makeText(this,"Tag  writen", Toast.LENGTH_LONG).show();
        }catch(Exception e){
        Log.e("FormatTag", e.getMessage());
        }

    }
    private NdefRecord createTextRecord(String content){
        try{
            byte[]language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);
            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
        }catch(Exception e){
            Log.e("CreateTextRecord", e.getMessage());
        }
        return null;
    }
    private NdefMessage createNdefMessage(String content){
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage((new NdefRecord[]{ ndefRecord}));

        return ndefMessage;
    }
    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){
        try{
           if(tag == null){
               Toast.makeText(this,"Tag object cannot be null", Toast.LENGTH_LONG).show();
               return;
           }
            Ndef ndef = Ndef.get(tag);

            if(ndef == null){
                //format tag with the ndef format and writes the message
                formatTag(tag,ndefMessage);
            }else{
                ndef.connect();
                if(!ndef.isWritable()){
                    Toast.makeText(this,"Tag is not writable", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this,"Tag writen", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Log.e("writeNdefMessage", e.getMessage());
        }
    }
    private void enableforegroundDispatchSystem(){
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] intentFilters = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);

    }
    private void disableforegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
           }


    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this,"on Resume", Toast.LENGTH_LONG).show();
//        if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)){
//            NdefMessage ndefMessage = this.getNdefMessageFromIntent(getIntent());
//        }
        NdefMessage[] msgs = null;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Toast.makeText(this,"DISCOVERED", Toast.LENGTH_LONG).show();
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(rawMsgs != null && rawMsgs.length > 0){
                readTextFromMessage((NdefMessage)rawMsgs[0]);
            }
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                }
//            }
//            NdefRecord[] recs = msgs[0].getRecords();
//            String payload = new String(recs[0].getPayload());

//            txtTagContent.setText(payload);
//            byte[] type = recs[0].getType();
//            byte[] value = recs[0].getPayload();
        }
        enableforegroundDispatchSystem();
        Toast.makeText(this,"AFTER ENABLEFOREGROUND", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableforegroundDispatchSystem();
    }
    public void tglReadWriteOnClick(View view){
        txtTagContent.setText("");
    }

    public String getTextFromNdefRecord(NdefRecord ndefRecord){
        String tagContent =  null;
        try{
            byte[] payload = ndefRecord.getPayload();
            String textEnconding = ((payload[0] & 128) == 0)? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEnconding);
        }catch (UnsupportedEncodingException e){
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

}
