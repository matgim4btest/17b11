package rs.edu.matgim.zadatak;

public class Program {

    public static void main(String[] args) {

        DB _db = new DB();
        //_db.printFilijala();
        _db.printAktivniRacuni();
        
        _db.zadatak(1, 6, 1000.0f);
        _db.printAktivniRacuni();
    }
}
