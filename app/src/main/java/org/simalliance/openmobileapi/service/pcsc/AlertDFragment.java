package org.simalliance.openmobileapi.service.pcsc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;

public class AlertDFragment extends DialogFragment{
	
	public static AlertDFragment newInstance(String message) {
		AlertDFragment frag = new AlertDFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		String message = getArguments().getString("message");

		return new AlertDialog.Builder(getActivity())
		// Set Dialog Title
		.setTitle("Error")
		// Set Dialog Message
		.setMessage(message) 
		.setNeutralButton("Close", null)
		.create();
	}

}
