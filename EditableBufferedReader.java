import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class EditableBufferedReader extends BufferedReader {

    static final int DRETA = 67; //La fletxa dreta es representa al terminal 
    // com "^[[C", de tal manera que representarem DRETA com 'C'
    static final int ESQUERRA = 68; //La fletxa esquerra es representa al 
    // terminal com "^[[D", de tal manera que representarem ESQUERRA com 'D'
    static final int INICI = 72; //Tecla inicio Printea ^[[H, utilitzarem la H
    static final int FINAL = 70; //Tecla fin Printea ^[[F, utilitzarem la F
    static final int DEL = 51; //Tecla supr Printea ^[[3~, utilitzarem el 3
    static final int BPSK = 127; //Tecla <-- backspace, te numero en ASCII
    static final int INSERT = 50; //Tecla insert Printea ^[[2~, utilitzarem el 2
    static final int ENTER = 13; //Útil per saber quan s'introdueix un CR i hem d'acabar
   
    static final int VIRGULILLA = 126; //Útil per fer DEL e INSERT, ja que cal "~"
    static final int ESC = 27; //Útil per fer les fletxes, ja que ens cal "^["
    static final int CORXET = 91; //Útile per representar les fletxes ja que ens cal "["

    //Definim els valors a retornar per la funció read, escollim valors a partir del
    //256, ja que son valors lliures a la taula ASCII, no definim BPSK, ja que es un 
    //valor existent i el podem retornar a ell mateix
    static final int RET_DRETA = 256;
    static final int RET_ESQUERRA = 257;
    static final int RET_INICI = 258;
    static final int RET_FINAL = 259;
    static final int RET_DEL = 260;
    static final int RTE_INSERT = 261;

    //Varibale per poder usar metodes de la classe Line()
    Line line;
    

    EditableBufferedReader(InputStreamReader in){
        super(in);
        this.line = new Line();
    }
    public void setRaw() throws IOException {
        //Passar de mode Cooked a mode Raw
        //String amb la seqüència necessaria per canviar de mode Cooked a mode Raw al terminal i treutre l'echo
        String [] modeRaw = {"/bin/sh", "-c", "stty -echo raw </dev/tty"};
        try {
            //getRuntime().exec() serveix per poder executar la linea de comandes
            //waitFor() espera hasta que el subproceso termine
            Runtime.getRuntime().exec(modeRaw).waitFor();  
        }catch (Exception e) {
            //Comuniquem l'error
            System.out.println("Error");
        } 
    }
    public void unsetRaw() throws IOException {
        //Passar de mode Raw a mode Cooked
        //String amb la seqüència necessaria per canviar de mode Raw a mode Cooked al terminal
        String[] modeCooked = {"/bin/sh", "-c", "stty cooked <dev/tty"};
        try{
            //getRuntime().exec() serveix per poder executar la linea de comandes
            //waitFor() espera hasta que el subproceso termine
            Runtime.getRuntime().exec(modeCooked).waitFor();
        } catch (IOException | InterruptedException e){
            //Comuniquem l'error
            System.out.println("Error");
        }
    }

    public int read() throws IOException {
        int lectura = 0;

        //Llegim el caràcter amb la funció main de BufferedReader
        lectura = super.read();
        //Mirem si els caràcters enviats són ^[[
        if(lectura == ESC){
            lectura = super.read();
            if(lectura == CORXET){
                lectura = super.read();
                switch(lectura){
                    case DRETA:
                        return RET_DRETA;
                    case ESQUERRA:
                        return RET_ESQUERRA;
                    case INICI:
                        return RET_INICI;
                    case FINAL:
                        return RET_FINAL;
                    case INSERT:
                        //Mirem si el caràcter enviat és ~
                        if(super.read() == VIRGULILLA){
                            return RTE_INSERT;
                        }
                        return -1;
                    case DEL:
                        //Mirem si el caràcter enviat és ~
                        if(super.read() == VIRGULILLA){
                            return RET_DEL;
                        }
                        return -1;
                    default:
                        //Si l'usuari prem ^[[ i un caràcter desconegut no retornem
                        return -1;
                }
            }
        //Especifiquem el cas del BPSK valor 127 (tecla espai)
        } else if (lectura == BPSK) {
            return BPSK;
        //Si s'introdueix un caràcter comú el retornem
        } else {
            return lectura;
        }
        //Per si hi hagués algun error o cas no contemplat
        return -1;
    }

    public String readLine() throws IOException {
        String linea = null;
        int lectura = 0;

        //Ens fiquem en mode raw perque funcioni el programa i poder llegir la linea correctament
        this.setRaw();

        while((lectura = this.read()) != ENTER){
            switch(lectura){
                case RET_DRETA:
                    this.line.dreta();
                    System.out.print("\u001b[1C");
                break;
                case RET_ESQUERRA:
                    this.line.esquerra();
                    System.out.print("\u001b[1D");
                break;
                case RET_INICI:
                    int aux = this.line.start();
                    //Anem al inici, movent-nos cap a l'esquerra les posicions del cursor retornat
                    System.out.print("\u001b[" + aux + "D");
                break;
                case RET_FINAL:
                    int aux = this.line.end();
                    //Anem al final, movent-nos cap a la dreta les posicions del cursor retornat
                    System.out.print("\u001b[" + aux + "C");
                break;
                case RET_DEL:
                    this.line.del();
                break;
                case RTE_INSERT:
                    this.line.ins();
                break;
                case BPSK:
                    this.line.bksp();
                break;
                default:
                    //Per convertir el int llegit a un char utilitzem (char) int
                    this.line.add((char) lectura);
                break;
            }
        }
        //Tornem al mode per defecte de la consola
        this.unsetRaw();
        linea = line.toString();
        return linea;
    }
}
