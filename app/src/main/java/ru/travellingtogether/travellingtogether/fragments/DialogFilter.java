package ru.travellingtogether.travellingtogether.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ru.travellingtogether.travellingtogether.R;

public class DialogFilter extends android.app.DialogFragment {

    // variables for ets
    EditText filterFrom, filterTo;
    public FragmentTripList source = null;

    public DialogFilter(FragmentTripList ftl){
        source = ftl;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_filter, null);
        filterFrom = (EditText) v.findViewById(R.id.filterFrom);
        filterTo = (EditText) v.findViewById(R.id.filterTo);
        builder.setTitle("Filter trip list");
        builder.setView(v);
        builder.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get from and to values and update trip list
                String from = filterFrom.getText().toString();
                String to = filterTo.getText().toString();
                if (!from.equals("") || !to.equals("")) {
                    source.updateList(from, to);
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), R.string.fillOneField, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCancel(dialog);
            }
        });

        return builder.create();
    }
}