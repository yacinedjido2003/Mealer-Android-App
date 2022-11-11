package com.example.mealerapp;

import static android.util.Patterns.EMAIL_ADDRESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private  TextView signUp,logOut;
    private EditText editTextAdresseCourriel, editTextPassword;
    private FirebaseAuth mAuth;
    private String uid;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);//C'est quoi saved instances ?
        setContentView(R.layout.main_page);

        signUp = findViewById(R.id.signupButton);
        signUp.setOnClickListener(this);
        logOut= findViewById(R.id.loginButton);
        logOut.setOnClickListener(this);
        

        editTextAdresseCourriel = findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.LogInPassword);
        mAuth = FirebaseAuth.getInstance();
    }



    public void onClick(View view){
        if(view.getId()==R.id.signupButton){
            startActivity(new Intent(this, signup_activity.class));
        }


       // Login
        if(view.getId()==R.id.loginButton){
            UserLogin();
        }

        //Logout
        if(view.getId()==R.id.logoutButton){
         UserLogout();
        }
    }
    private void UserLogout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    private void UserLogin() {
        //Getting information and trimming it to a string
        String MotDePasse = editTextPassword.getText().toString().trim();
        String adressecourriel= editTextAdresseCourriel.getText().toString().trim();

        //Si le champ du courriel est vide
        if (adressecourriel.isEmpty()) {
            editTextAdresseCourriel.setError("Votre email est requis.");
            editTextAdresseCourriel.requestFocus();
            return;
        }
        //Si le courriel est non valide
        if (!EMAIL_ADDRESS.matcher(adressecourriel).matches()) {
            editTextAdresseCourriel.setError("Votre email est non valide.");
            editTextAdresseCourriel.requestFocus();
            return;
        }

        //Si le champ du mot de passe est vide
        if (MotDePasse.isEmpty()) {
            editTextPassword.setError("Votre mot de passse est requis");
           editTextPassword.requestFocus();
            return;
        }
        //Si le mot de passe est non valide
        if (MotDePasse.length() < 8) {
            editTextPassword.setError(" Mot de passse est court: il faut 8 caractères au minimum");
            editTextPassword.requestFocus();
            return;

        }

        //signing In a user using his firebase Credentials
        mAuth.signInWithEmailAndPassword(adressecourriel,MotDePasse).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //getting user userType and use it to sign him in

                    //String user ? Il n'est utilisé nul par apparemment, il sert à quoi?
                    String user =task.getResult().getUser().getUid();

                    //On va regarder dans la table Users dans firebase
                    FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user=snapshot.getValue(User.class);

                            //Check le type d'utilisateur et le diriger vers sa page
                            if(user.getUserType().equals("Client")){
                                //diriger vers profil utilisateur client
                                startActivity(new Intent(MainActivity.this, client_page_activity.class));
                            }
                            else if(user.getUserType().equals("Cooker")){
                                //diriger vers profil utilisateur cooker
                                startActivity(new Intent(MainActivity.this, cooker_page_activity.class));
                            }
                            else if(user.getUserType().equals("Administrator")){
                                //diriger vers profil utilisateur admin
                                startActivity(new Intent(MainActivity.this, admin_page_activity.class));
                            }
                        }
                        //Nothing
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }else{
                    Toast.makeText(MainActivity.this, "Failed to login check your credentials", Toast.LENGTH_LONG).show();
                }
            }
        }
        );
    }
}