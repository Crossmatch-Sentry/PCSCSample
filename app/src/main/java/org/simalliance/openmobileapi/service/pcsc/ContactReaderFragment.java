package org.simalliance.openmobileapi.service.pcsc;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ContactReaderFragment extends DialogFragment{
	
	private String selectedReader="";
	private ArrayList<String> mlist;

	public static ContactReaderFragment newInstance(ArrayList<String> al) {
		ContactReaderFragment frag = new ContactReaderFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("ar",al);
        frag.setArguments(args);
        return frag;
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mlist = getArguments().getStringArrayList("ar");
		String[] strArry = new String[mlist.size()];
		strArry = mlist.toArray(strArry);

		this.setCancelable(false);
		
		return new AlertDialog.Builder(getActivity())
		// Set Dialog Title
		.setTitle("SelectReader")
		.setItems(strArry, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which)
			{
				selectedReader=mlist.get(which);
				mListener.onDialogSelectClick(ContactReaderFragment.this);
			}
		})
		.create();
	}
	
	public interface ContactReaderDialogListener{
		public void onDialogSelectClick(ContactReaderFragment dialog);
		
	}
	
	ContactReaderDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			mListener = (ContactReaderDialogListener)activity;
		} catch (ClassCastException e) {
			
		}
	}
	
	public String getSelectedReader() 
    {
   	 return selectedReader;
    }
}
