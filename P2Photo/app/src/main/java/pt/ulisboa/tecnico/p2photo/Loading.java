package pt.ulisboa.tecnico.p2photo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

public class Loading extends AppCompatActivity {

    int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        flag = 1;

        myTask myTask = new myTask();
        myTask.execute();

        while(flag==1) {
            //wait
        }

        Toast.makeText(getApplicationContext(), "You have been sucessfully registered!", Toast.LENGTH_SHORT).show();
        Loading.this.finish();
    }

    class myTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params)  {
            try {
                String hostname = "10.0.2.2";

                System.out.println("entra aqui");
                Socket socket = new Socket(hostname, 5111);
                System.out.println(socket.getInetAddress().getHostAddress());
                Communications communication = new Communications(socket);

                Intent intent = new Intent();
                Bundle extras = intent.getExtras();

                String command = "LUSIADAS";
                String lusiadas = "As armas e os baroes assinalados,Que da ocidental praia Lusitana,Por mares nunca de antes navegados,Passaram ainda alem da Taprobana,Em perigos e guerrasesforcados,Mais do que prometia a forca humana,E entre gente remota edificaramNovo Reino, que tanto sublimaram;2E tambem as memorias gloriosasDaqueles Reis, que foram dilatandoA Fe, o Imperio, e as terras viciosasDe Africa e de Asia andaram devastando;E aqueles, que por obras valerosasSe vao da lei da morte libertando;Cantando espalharei por toda parte,Se a tanto me ajudar o engenho e arte.Cessem do sabio Grego e do TroianoAs navegacoes grandes que fizeram;Cale-se de Alexandro e de TrajanoA fama das vitorias que tiveram;Que eu canto o peito ilustre Lusitano,A quem Neptuno e Marte obedeceram:Cesse tudo o que a Musa antigua canta,Que outro valor mais alto se alevanta.E vos, Tagides minhas, pois criadoTendes em mim um novo engenho ardente,Se sempre em verso humilde celebradoFoi de mim vosso rio alegremente,Dai-me agora um som alto e sublimado,Um estilo grandiloquo e corrente,Porque de vossas aguas, Febo ordeneQue nao tenham inveja as de Hipoerene.Dai-me uma furia grande e sonorosa,E nao de agreste avena ou frauta ruda,Mas de tuba canora e belicosa,Que o peito acende e a cor ao gesto muda;Dai-me igual canto aos feitos da famosaGente vossa, que a Marte tanto ajuda;Que se espalhe e se cante no universo,Se tao sublime preco cabe em verso.6E vos, o bem nascida segurancaDa Lusitana antigua liberdade,E nao menos certissima esperancaDe aumento da pequena Cristandade;Vos, o novo temor da Maura lanca,Maravilha fatal da nossa idade,Dada ao mundo por Deus, que todo o mande,Para do mundo a Deus dar parte grande;7";

                communication.sendInChunks(command);
                communication.sendInChunks(lusiadas);
                flag = 0;

            } catch(UnknownHostException uhe) {
                uhe.printStackTrace();
                System.out.println("Couldn't find the host.");
            } catch(IOException ioe) {
                ioe.printStackTrace();
                System.out.println("IOException");
            } catch(CommunicationsException ce) {
                ce.printStackTrace();
                System.out.println("CommunicationsException");
            }
        return null;}
    }

}
