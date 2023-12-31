import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
//import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Plataforma934 {
    private ArrayList<EmpresaTransporte> empresas;
    private ArrayList<Pasajero> pasajeros;

    public Plataforma934() {
        empresas = new ArrayList<>();
        pasajeros = new ArrayList<>();
    }

    public void agregarEmpresa(EmpresaTransporte empresanueva) {
        empresas.add(empresanueva);
    }

    public void registrarPasajero(String apellido, String nombre, int dni, String claveAcceso, String tarjetaCredito) {
        if (validarClaveAcceso(claveAcceso)) {
            Pasajero pasajero = new Pasajero(apellido, nombre, dni, claveAcceso);
            if (tarjetaCredito != null) {
                pasajero.setTarjeta(tarjetaCredito);
            }
            pasajeros.add(pasajero);
        } else {
            System.out.println("La clave de acceso no cumple los requisitos de seguridad.");
        }
    }

    private boolean validarClaveAcceso(String claveAcceso) {
        // Validar que la clave cumple con los requisitos (8 caracteres mínimo, una minúscula, una mayúscula y un número)
        // Retorna true si cumple los requisitos, false en caso contrario.
        return claveAcceso.length() >= 8;
    }

    public boolean comprarPasaje(Pasajero pasajero, Servicio serv) {
        if (validarPago(pasajero)) {
            if (serv.getAsientosDisponibles() > 0) {
                int reserva = nroReserva(serv);
                if(serv.getAsientos().get(reserva).isDisponible()){
                    serv.reservar(pasajero,reserva);
                    pasajero.agregarPasaje(new Pasaje(serv.getOrigen(),serv.getDestino(),serv.getFechaViaje(),serv.getCosto()));
                    generarPDF(serv.getOrigen(),serv.getDestino(),Integer.toString(reserva+1),serv.getFechaViaje(),pasajero.getNombre()+" "+pasajero.getApellido(),Integer.toString(pasajero.getDni()));
                    return true;
                }
            }
        }
        return false;
    }

    public int nroReserva( Servicio serv){
        String result;
        System.out.println("Las butacas disponibles son: ");
        ArrayList<Asiento> asientos = serv.getAsientos();
        for(int i=0; i< asientos.size(); i++){
            if(asientos.get(i).isDisponible()) {
                System.out.print("[" + (i+1) + "]");
            }
        }
        Scanner leer = new Scanner(System.in);
        do{
            System.out.println("Seleccione una butaca: ");
            result = leer.nextLine();
        }while(!isNumeric(result));

        return Integer.parseInt(result)-1;
    }

    public boolean validarPago(Pasajero p1) {
        //valida que el pasajero tenga plata,se haga el pago, etc.

        //En este caso se fija que tenga tarjeta nomas.
        return !(p1.getTarjeta().equals(""));
    }

    public void mostrarViajes(ArrayList<Servicio> servicios, Pasajero p1) {
        this.mostrarViajesPantalla(servicios);
        Scanner leer = new Scanner(System.in);
        System.out.println("Si quiere comprar uno de los servicios ingrese la letras s o S:");
        String opcion = leer.nextLine();
        if (opcion.equalsIgnoreCase("s")) {
            System.out.println("Ingrese el número del Servicio que de desea comprar: ");
            opcion = leer.nextLine();
            if (isNumeric(opcion)) {
                int op = Integer.parseInt(opcion) -1;
                if (op >= 0 && op < servicios.size()) {
                    if(comprarPasaje(p1, servicios.get(op)))
                        System.out.println("Boleto comprado");
                    else
                        System.out.println("Boleto NO comprado");

                }
            }
        }
    }

    public void mostrarViajesPantalla(ArrayList<Servicio> servicios) {
        for (int i = 0; i < servicios.size(); i++) {
            Servicio aux = servicios.get(i);
            System.out.println("Servicio: " + (i + 1));
            System.out.println("Origen: " + aux.getOrigen() + " | " +
                    "Destino " + aux.getDestino() + " | " +
                    "Cantidad de Asientos: " + aux.getCantAsientos() + " | " +
                    "Cantidad de Asientos disponibles: " + aux.getAsientosDisponibles() + " | " +
                    "Costo: " + aux.getCosto());
        }
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }

    public ArrayList<Servicio> buscarServicios(String origen, String destino /* ,LocalDate fechaViaje */) {
        ArrayList<Servicio> serviciosDisponibles = new ArrayList<>();

        for (EmpresaTransporte empresa : empresas) {
            ArrayList<Servicio> servicios = empresa.buscarServicios(origen, destino /*, fechaViaje*/);
            serviciosDisponibles.addAll(servicios);
        }
        if (serviciosDisponibles.isEmpty()){
            System.out.println("No existen viajes disponibles");
        }
        return serviciosDisponibles;
    }

    public void generarPDF(String origen, String destino, String butaca, LocalDate fecha, String NomYApell, String dni){

        try {
            Document document = new Document();
            int num = (int)(Math.random()*10000+1);
            String dest = "pdf/boleto"+ Integer.toString(num) +".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(dest));
            document.open();

            document.addTitle("Boleto");

            Paragraph u = new Paragraph("Plataforma 9 y 3/4");
            u.setAlignment(Element.ALIGN_CENTER);
            document.add(u);

            document.add(new Paragraph("_____________________________________________________________________________"));
            document.add(new Paragraph("Boleto"));
            document.add(new Paragraph("Origen: "+origen));
            document.add(new Paragraph("Destino: "+destino));
            document.add(new Paragraph("Fecha: "+fecha));
            document.add(new Paragraph("Butaca: "+butaca));
            document.add(new Paragraph("Pasajero:"));
            document.add(new Paragraph("     "+"Dni: "+dni));
            document.add(new Paragraph("     "+"Nombre y apellido: "+NomYApell));
            document.add(new Paragraph("_____________________________________________________________________________"));


            document.close();

            System.out.println("PDF creado");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Plataforma934.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    public void listarPasajes(Pasajero pasajero){
        ArrayList<Pasaje> aux = pasajero.getPasajes();
        if (aux.isEmpty()){
            System.out.println("No se registraron pasajes comprados");
        }else {
            System.out.println("Se tienen registrados los siguientes pasajes: ");
            System.out.println("--------------------------------------------------------------------------------");
            for (int i=0;i<aux.size();i++){
                System.out.println("Pasaje numero:"+ i);
                System.out.println("Origen: " + aux.get(i).getOrigen());
                System.out.println("Destino: " + aux.get(i).getDestino());
                System.out.println("Fecha de viaje: " + aux.get(i).getFechaViaje());
                //System.out.println("Cantidad de asientos: "+ aux.get(i).getCantAsientos());
                System.out.println("Costo: " + aux.get(i).getCosto());
                System.out.println("--------------------------------------------------------------------------------");
            }
        }


    }
    public void suscribirViajeImprovisado(Pasajero pasajero, String origen, String destino) {
        // Suscribir al pasajero para recibir alertas de pasajes con descuento para el viaje improvisado
    }

    public void generarEstadisticas(Pasajero pasajero) {
        // Generar estadísticas a partir de los datos de los pasajes del pasajero
    }

    public void devolverPasaje(Pasajero pasajero, Pasaje pasaje) {
        // Calcular el porcentaje de devolución según la antelación a la fecha del viaje
        // Realizar la devolución a la tarjeta de crédito del pasajero
        // Informar a la empresa de transporte sobre la devolución del pasaje
    }

    public static void main(String[] args) {
        ArrayList<Asiento> asientos1 = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            asientos1.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos2 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos2.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos3 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos3.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos4 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos4.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos5 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos5.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos6 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos6.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos7 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos7.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos8 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos8.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos9 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos9.add(new Asiento(i));
        }
        ArrayList<Asiento> asientos10 = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            asientos10.add(new Asiento(i));
        }
        EmpresaTransporte viatac = new EmpresaTransporte("ViaTac");
        EmpresaTransporte rapido = new EmpresaTransporte("El Rapido");
        EmpresaTransporte condor = new EmpresaTransporte("El Condor");
        Servicio servicio1 = new Servicio("Buenos Aires", "Córdoba", LocalDate.of(2023, 6, 20), asientos1, 15000.0);
        Servicio servicio2 = new Servicio("Mendoza", "Bariloche", LocalDate.of(2023, 7, 5), asientos2, 2000.0);
        Servicio servicio3 = new Servicio("Rosario", "Salta", LocalDate.of(2023, 8, 15), asientos3, 5000.0);
        Servicio servicio4 = new Servicio("Mar del Plata", "Neuquén", LocalDate.of(2023, 9, 1), asientos4, 8000.0);
        Servicio servicio5 = new Servicio("Córdoba", "Buenos Aires", LocalDate.of(2023, 6, 25), asientos5, 12000.0);
        Servicio servicio6 = new Servicio("Bariloche", "Mendoza", LocalDate.of(2023, 7, 10), asientos6, 3000.0);
        Servicio servicio7 = new Servicio("Salta", "Rosario", LocalDate.of(2023, 8, 20), asientos7, 5500.0);
        Servicio servicio8 = new Servicio("Neuquén", "Mar del Plata", LocalDate.of(2023, 9, 5), asientos8, 7500.0);
        Servicio servicio9 = new Servicio("Buenos Aires", "Rosario", LocalDate.of(2023, 6, 30), asientos9, 10000.0);
        Servicio servicio10 = new Servicio("Córdoba", "Mendoza", LocalDate.of(2023, 7, 15), asientos10, 1800.0);
        viatac.agregarServicio(servicio1);
        viatac.agregarServicio(servicio2);
        viatac.agregarServicio(servicio5);
        rapido.agregarServicio(servicio3);
        rapido.agregarServicio(servicio4);
        rapido.agregarServicio(servicio1);
        rapido.agregarServicio(servicio6);
        rapido.agregarServicio(servicio2);
        rapido.agregarServicio(servicio7);
        condor.agregarServicio(servicio8);
        condor.agregarServicio(servicio1);
        condor.agregarServicio(servicio3);
        condor.agregarServicio(servicio2);
        condor.agregarServicio(servicio9);
        condor.agregarServicio(servicio10);
        Plataforma934 sistema = new Plataforma934();
        sistema.registrarPasajero("af", "Matias", 1, "aaaaaaaaaaaaa", "visa");
        sistema.registrarPasajero("as", "Martin", 2, "sddddddsadsda", "Visa");
        sistema.registrarPasajero("asdas", "opo", 3, "asdoiahfaohas", "Visa");
        sistema.registrarPasajero("fas v", "Pipa", 4, "asda sasfasfa", "Visa");
        sistema.registrarPasajero("asdas", "Paco", 5, "liloloalals", "Visa");
        sistema.agregarEmpresa(viatac);
        sistema.agregarEmpresa(rapido);
        sistema.agregarEmpresa(condor);
        Pasajero logeado = new Pasajero("lola", "lolo", 10, "contrasenia");
        //ArrayList<Servicio> busqueda = sistema.buscarServicios("Buenos Aires", "Córdoba", LocalDate.of(2023, 6, 20));
        //sistema.mostrarViajesPantalla(busqueda);
        //sistema.mostrarViajes(busqueda,logeado);
        boolean salir = false;
        Scanner sn = new Scanner(System.in), teclado = new Scanner(System.in), sn2 = new Scanner(System.in);
        int opcion = 0, tries = 0, dni;

        while (!salir) {

            System.out.println("---Bienvenido al sistema de La empresa Plataforma 9-3/4---");
            System.out.println("elija su rol");
            System.out.println("1. INICIAR SESION/cliente");
            System.out.println("2. iniciar sesion/admin");
            System.out.println("3. Buscar pasaje");
            System.out.println("4. Listar pasajes comprados");
            System.out.println("5. Salir");

            opcion = sn.nextInt();

            switch (opcion) {
                case 1:
                    tries = 3;
                    System.out.println("INGRESE SU DOCUMENTO");
                    dni = sn2.nextInt();
                    System.out.println("INGRESE SU CONTRASEÑA");
                    String contrasenia = teclado.nextLine();
                    //Deberia verificar
                    System.out.println("Logueado con exito");

                    break;
                case 2:
                    System.out.println("INGRESE SU DOCUMENTO");
                    dni = sn2.nextInt();
                    System.out.println("INGRESE SU CONTRASEÑA");
                    String contra = teclado.nextLine();
                    //Deberia verificar
                    System.out.println("Logueado con exito");
                    break;
                case 3:
                    System.out.print("Ingrese Origen del viaje: ");
                    String string1 = teclado.nextLine();
                    System.out.print("Ingrese destino del viaje: ");
                    String string2 = teclado.nextLine();
                    //System.out.print("Ingrese la fecha (dd/mm/yy): ");
                    //String fechaString = teclado.nextLine();
                    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YY/MM/dd");
                    //LocalDate fecha = LocalDate.parse(fechaString, formatter);
                    ArrayList<Servicio> busqueda = sistema.buscarServicios(string1, string2);
                    logeado.setTarjeta("123456789");
                    if (!busqueda.isEmpty()){
                        sistema.mostrarViajes(busqueda, logeado);
                    }
                    break;
                case 4:
                    sistema.listarPasajes(logeado);
                    break;
                case 5:
                    salir = true;
                    System.out.println("Saliendo del menú...");
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
                    break;
            }
        }
    }
}
