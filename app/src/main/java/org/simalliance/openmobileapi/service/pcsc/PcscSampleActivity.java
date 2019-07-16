package org.simalliance.openmobileapi.service.pcsc;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.simalliance.openmobileapi.service.pcsc.PcscException;
import org.simalliance.openmobileapi.service.pcsc.PcscJni;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.Disposition;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.Protocol;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.ReaderState;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.Scope;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.ShareMode;
import org.simalliance.openmobileapi.service.pcsc.PcscJni.Status;
//import org.simalliance.openmobileapi.service.pcsc.pcscsample.R;

import java.util.ArrayList;

public class PcscSampleActivity extends AppCompatActivity implements Runnable, ContactReaderFragment.ContactReaderDialogListener {

    TextView tv = null;
    Button btn;
    Thread t;

    long context = 0;
    String reader = "";
    String[] terminals = null;
    Handler mHandler;
    ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcsc_sample);

        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);
        btn = findViewById(R.id.button1);
        tv = findViewById(R.id.console);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearText();
                startThread ();
            }
        });

        mHandler = new Handler();
        startThread ();
    }

    /* Display an error dialog */
    private void showErrorAlert(String errorMessage) {
        final String errMess = errorMessage;
        mHandler.post(new Runnable() {
            public void run() {

                AlertDFragment newAlertFrag = AlertDFragment.newInstance(errMess);
                newAlertFrag.show(getFragmentManager(), "Alertdialog");
                //new AlertDialog.Builder(PcscSampleActivity.this)
                //.setTitle("Error")
                //.setMessage(errMess)
                //.setNeutralButton("Close", null)
                //.show();
            }
        });
    }

    /* Display an error dialog */
    private void showReaderDialog(ArrayList<String> errorMessage) {
        final ArrayList<String> errMess = errorMessage;
        mHandler.post(new Runnable() {
            public void run() {

                ContactReaderFragment newReaderFrag = ContactReaderFragment.newInstance(errMess);
                newReaderFrag.show(getFragmentManager(), "Readerdialog");

            }
        });
    }

    private void shutdown(Exception ex) {
        logText("PcscException: " + ex.getMessage() + "\n");
        //showErrorAlert(ex.getMessage());
        try {
            if (context != 0)
                PcscJni.releaseContext(context);
        } catch (PcscException e) {
            e.printStackTrace();
        }
        showErrorAlert(ex.getMessage());
    }

    private String status(int state) {
        if ((state & ReaderState.Empty) == ReaderState.Empty)
            return "absent";

        if ((state & ReaderState.Present) == ReaderState.Present)
            return "present";

        if ((state & ReaderState.Exclusive) == ReaderState.Exclusive)
            return "exclusive";

        return "unknown";
    }

    @Override
    public void onDialogSelectClick(ContactReaderFragment dialog)
    {
        String tempStr = dialog.getSelectedReader();
        if (tempStr.length() > 0)
        {
            //logText("selected reader: " + tempStr + "\n");
            readFromSelectedReader(tempStr);
        }
    }

    private void clearText() {
        mHandler.post(new Runnable() {
            public void run() {
                tv.setText("");
            }
        });
    }

    /* log content and scroll to bottom */
    private void logText(String message) {
        final String mess = message;
        mHandler.post(new Runnable() {
            public void run() {
                tv.append(mess);
            }
        });

        //tv.append(message);
    }

    private void setSpinner(final boolean on) {
        mHandler.post(new Runnable() {
            public void run() {
                if (on)
                    spinner.setVisibility(View.VISIBLE);
                else
                    spinner.setVisibility(View.GONE);
            }
        });
    }

    public void startThread ()
    {
        t = new Thread(this);
        t.start();
    }

    public void run() {

        logText("PC/SC Test Application\n---\n");
           /* SCardEstablishContext */
        logText("SCardEstablishContext: ");
        setSpinner(true);
        try {
            context = PcscJni.establishContext(Scope.User);
            logText("ok: " + context + "\n");
        } catch (PcscException ex) {
            logText("PcscException: " + ex.getMessage() + "\n");
            showErrorAlert(ex.getMessage());
            return;
        }
        setSpinner(false);


       	/* SCardListReaders */
        ArrayList<String> listArr = new ArrayList<String>();
        logText("\nSCardListReaders: ");
        try {
            terminals = PcscJni.listReaders(context, null);
            int cntReader=1;
            for (String terminal: terminals)
            {
                listArr.add(terminal);
                logText("\n" + cntReader + "- " + terminal);
                cntReader++;
            }
            logText("\n");
            if (terminals.length > 0) {
                reader = terminals[0];
            }
        } catch (PcscException ex) {
            shutdown(ex);
            return;
        }

        if (listArr.isEmpty()== false)
            showReaderDialog(listArr);
        else
            logText("No Readers found. ReScan to try again.\n");


    }

    public void readFromSelectedReader(String readerSel)
    {
        if (context == 0)
        {
            showErrorAlert("Context is zero. Have not established context\n");
            return;
        }
		/* SCardGetStatusChange for ALL attached readers*/
        logText("\nSCardGetStatusChange:");
        //for (int j=0; j < terminals.length; j++) {
        reader = readerSel; //terminals[j];
        int[] status = new int[] { Status.Unknown, 0 };
        byte[] atr;
        try {
            atr = PcscJni.getStatusChange(context, 0, reader, status);
        } catch (PcscException ex) {
            shutdown(ex);
            return;
        }
        if (atr==null) {
            logText("\nNo card on reader: "+reader);
        } else {
            logText("\nFound card on reader: "+reader);
            StringBuffer string = new StringBuffer();
            for (int i = 0; atr != null && i < atr.length; ++i)
                string.append(Integer.toHexString(0x0100 + (atr[i] & 0x00FF)).substring(1));
            logText("\nATR: " + string.toString());
            logText("\nStatus: " + status(status[0]));
            //break;	/* found a card, get out of loop now */
        }
        //}


        /* SCardConnect */
        logText("\nSCardConnect: ");
        long card = 0;
        int[] protocol = new int[] { Protocol.T0 | Protocol.T1 };
        try {
            card = PcscJni.connect(context, reader, ShareMode.Shared, protocol);
            logText("ok - protocol: " + ((protocol[0] == Protocol.T0) ? "T=0" : "T=1") + "\n");
        } catch (PcscException ex) {
            shutdown(ex);
            return;
        }


		/* SCardTransmit */
        logText("\nSCardTransmit: ");
        try {
            byte[] response = PcscJni.transmit(card, protocol[0], new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });

            logText("\n-> a0a4040000\n");
            StringBuffer string = new StringBuffer();
            for (int i = 0; response != null && i < response.length; ++i)
                string.append(Integer.toHexString(0x0100 + (response[i] & 0x00FF)).substring(1));
            logText("<- " + string.toString() + "\n");
        } catch (PcscException ex) {
            shutdown(ex);
            return;
        }


		/* SCardDisconnect */
        logText("\nSCardDisconnect()\n");
        try {
            // don't do Disposition.Reset or card is unpowered!
            PcscJni.disconnect(card, Disposition.Leave);
        } catch (Exception ex) {
            showErrorAlert("Exception - " + ex.getMessage() + "\n");
            return;
        }


		/* SCardReleaseContext */
        logText("\nSCardReleaseContext: ");
        try {
            PcscJni.releaseContext(context);
            logText("ok\n");
        } catch (PcscException ex) {
            logText("PcscException: " + ex.getMessage() + "\n");
            showErrorAlert(ex.getMessage());
        }

        context = 0;
    }

}
