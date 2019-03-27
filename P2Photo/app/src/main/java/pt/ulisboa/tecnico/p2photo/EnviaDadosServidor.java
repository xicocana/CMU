package pt.ulisboa.tecnico.p2photo;

import java.io.IOException;
import java.net.Socket;

import pt.ulisboa.tecnico.p2photo.exceptions.CommunicationsException;

/**
 * Created by ist182069 on 27-03-2019.
 */

public class EnviaDadosServidor {

    public static void main(String[] args) throws IOException, CommunicationsException {

        System.out.println("entra aqui");
        Socket socket = new Socket("localhost", 5111);
        System.out.println(socket.getInetAddress().getHostAddress());
        Communications communication = new Communications(socket);

        String command = "LUSIADAS";
        String lusiadas = "As armas e os baroes assinalados,Que da ocidental praia Lusitana,Por mares nunca de antes navegados,Passaram ainda alem da Taprobana,Em perigos e guerrasesforcados,Mais do que prometia a forca humana,E entre gente remota edificaramNovo Reino, que tanto sublimaram;2E tambem as memorias gloriosasDaqueles Reis, que foram dilatandoA Fe, o Imperio, e as terras viciosasDe Africa e de Asia andaram devastando;E aqueles, que por obras valerosasSe vao da lei da morte libertando;Cantando espalharei por toda parte,Se a tanto me ajudar o engenho e arte.Cessem do sabio Grego e do TroianoAs navegacoes grandes que fizeram;Cale-se de Alexandro e de TrajanoA fama das vitorias que tiveram;Que eu canto o peito ilustre Lusitano,A quem Neptuno e Marte obedeceram:Cesse tudo o que a Musa antigua canta,Que outro valor mais alto se alevanta.E vos, Tagides minhas, pois criadoTendes em mim um novo engenho ardente,Se sempre em verso humilde celebradoFoi de mim vosso rio alegremente,Dai-me agora um som alto e sublimado,Um estilo grandiloquo e corrente,Porque de vossas aguas, Febo ordeneQue nao tenham inveja as de Hipoerene.Dai-me uma furia grande e sonorosa,E nao de agreste avena ou frauta ruda,Mas de tuba canora e belicosa,Que o peito acende e a cor ao gesto muda;Dai-me igual canto aos feitos da famosaGente vossa, que a Marte tanto ajuda;Que se espalhe e se cante no universo,Se tao sublime preco cabe em verso.6E vos, o bem nascida segurancaDa Lusitana antigua liberdade,E nao menos certissima esperancaDe aumento da pequena Cristandade;Vos, o novo temor da Maura lanca,Maravilha fatal da nossa idade,Dada ao mundo por Deus, que todo o mande,Para do mundo a Deus dar parte grande;7";

        communication.sendInChunks(command);
        communication.sendInChunks(lusiadas);

        communication.end();

    }
}