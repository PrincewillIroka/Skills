package skills.skills;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

public class UpdateSkillDialog extends DialogFragment {

    Button ok;
    CheckBox cleaner, computerEngineer, electrician, gardener, nurse, plumber;

    String activityName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View dataView = inflater.inflate(R.layout.update_skill_dialog, container);

        ok = dataView.findViewById(R.id.ok);
        cleaner = dataView.findViewById(R.id.cleaner);
        computerEngineer = dataView.findViewById(R.id.computerEngineer);
        electrician = dataView.findViewById(R.id.electrician);
        gardener = dataView.findViewById(R.id.gardener);
        nurse = dataView.findViewById(R.id.nurse);
        plumber = dataView.findViewById(R.id.plumber);

        activityName = getActivity().getLocalClassName().toString();


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickSkills();
            }
        });

        return dataView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public void pickSkills() {
        StringBuffer chosen = new StringBuffer();

        if (cleaner.isChecked()) {
            chosen.append(cleaner.getText().toString());
        }

        if (computerEngineer.isChecked()) {
            if (chosen.length() > 0) {
                chosen.append(", " + computerEngineer.getText().toString());
            } else {
                chosen.append(computerEngineer.getText().toString());
            }
        }

        if (electrician.isChecked()) {
            if (chosen.length() > 0) {
                chosen.append(", " + electrician.getText().toString());
            } else {
                chosen.append(electrician.getText().toString());
            }
        }

        if (gardener.isChecked()) {
            if (chosen.length() > 0) {
                chosen.append(", " + gardener.getText().toString());
            } else {
                chosen.append(gardener.getText().toString());
            }
        }

        if (nurse.isChecked()) {
            if (chosen.length() > 0) {
                chosen.append(", " + nurse.getText().toString());
            } else {
                chosen.append(nurse.getText().toString());
            }
        }

        if (plumber.isChecked()) {
            if (chosen.length() > 0) {
                chosen.append(", " + plumber.getText().toString());
            } else {
                chosen.append(plumber.getText().toString());
            }
        }

        switch (activityName) {
            case "Register":
                ((Register) getActivity()).user_skills.setText(chosen);
                break;
            case "UpdateAProfile":
                ((UpdateAProfile) getActivity()).skillView.setText(chosen);
                break;
        }

        getDialog().dismiss();
    }

} //End of CustomDialog
