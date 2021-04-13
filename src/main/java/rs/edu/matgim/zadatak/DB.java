package rs.edu.matgim.zadatak;
//1232234543
import java.sql.*;
import java.text.SimpleDateFormat;

public class DB {

    String connectionString = "jdbc:sqlite:src\\main\\java\\Banka.db";

    public void printFilijala() {
        try (Connection conn = DriverManager.getConnection(connectionString); Statement s = conn.createStatement()) {

            ResultSet rs = s.executeQuery("SELECT * FROM Filijala");
            while (rs.next()) {
                int IdFil = rs.getInt("IdFil");
                String Naziv = rs.getString("Naziv");
                String Adresa = rs.getString("Adresa");

                System.out.println(String.format("%d\t%s\t%s", IdFil, Naziv, Adresa));
            }

        } catch (SQLException ex) {
            System.out.println("Greska prilikom povezivanja na bazu");
            System.out.println(ex);
        }
    }

    public void printAktivniRacuni() {
        try (Connection conn = DriverManager.getConnection(connectionString); Statement s = conn.createStatement()) {

            ResultSet rs = s.executeQuery("SELECT * FROM Racun WHERE Status='A'");
            while (rs.next()) {
                int IdRac = rs.getInt("IdRac");
                String Status = rs.getString("Status");
                int BrojStavki = rs.getInt("BrojStavki");
                float DozvMinus = rs.getFloat("DozvMinus");
                float Stanje = rs.getFloat("Stanje");
                int IdKom = rs.getInt("IdKom");
                int IdFil = rs.getInt("IdFil");

                System.out.println(String.format("%d\t%s\t%d\t%f\t%f\t%d\t%d", IdRac, Status, BrojStavki, DozvMinus, Stanje, IdKom, IdFil));
            }

        } catch (SQLException ex) {
            System.out.println("Greska prilikom povezivanja na bazu");
            System.out.println(ex);
        }
    }

    public boolean zadatak(int idRacFrom, int idRacTo, float sum) {

        String upit = " SELECT  Stanje + DozvMinus AS Suma FROM Racun WHERE IdRac=?";

        try (Connection conn = DriverManager.getConnection(connectionString);
                PreparedStatement s = conn.prepareStatement(upit);) {
            conn.setAutoCommit(false);
            s.setInt(1, idRacFrom);
            ResultSet rs = s.executeQuery();
            rs.next();
            float maxMogucNovacZaPrenos = rs.getFloat("Suma");
            if (maxMogucNovacZaPrenos < sum) {
                System.out.println("Nije moguce"); // TODO: Obrisati na kraju
                return false;
            } else {
                String upitStavkaId = "SELECT MAX(IdSta)+1 FROM Stavka";
                String upitBrojStavki = "SELECT RedBroj+1 FROM Stavka WHERE IdRac=?";
                String upit1 = "INSERT INTO Stavka (IdSta, RedBroj, Datum, Vreme, Iznos, IdRac, IdFil) VALUES (?,?,?,?,?,?,?)";
                String upit2 = "INSERT INTO Uplata (IdSta, Osnov) VALUES (?,?)";
                String upit3 = "UPDATE Racun SET Stanje=Stanje+?, Status=(CASE WHEN Stanje+?>-DozvMinus THEN 'A' ELSE Status END) WHERE IdRac=?";
                try (Statement sUpitStavkaId = conn.createStatement();
                        PreparedStatement sBrojStavki = conn.prepareStatement(upitBrojStavki);
                        PreparedStatement s1 = conn.prepareStatement(upit1);
                        PreparedStatement s2 = conn.prepareStatement(upit2);
                        PreparedStatement s3 = conn.prepareStatement(upit3)) {

                    ResultSet uspitStavkaIdRes = sUpitStavkaId.executeQuery(upitStavkaId);
                    uspitStavkaIdRes.next();
                    int stavkaId = uspitStavkaIdRes.getInt(1);

                    sBrojStavki.setInt(1, idRacTo);
                    ResultSet uspitBrojStavkiRes = sBrojStavki.executeQuery();
                    int brojStavki = 1;
                    if (uspitBrojStavkiRes.next()) {
                        brojStavki = uspitBrojStavkiRes.getInt(1);
                    }

                    SimpleDateFormat formatterDatum = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat formatterVreme = new SimpleDateFormat("HH:mm");
                    Date date = new Date(System.currentTimeMillis());

                    s1.setInt(1, stavkaId);
                    s1.setInt(2, brojStavki);
                    s1.setString(3, formatterDatum.format(date));
                    s1.setString(4, formatterVreme.format(date));
                    s1.setFloat(5, sum);
                    s1.setInt(6, idRacTo);
                    s1.setInt(7, 1);

                    s1.execute();

                    s2.setInt(1, stavkaId);
                    s2.setString(2, "Prenos novca");
                    s2.execute();

                    s3.setFloat(1, sum);
                    s3.setFloat(2, sum);
                    s3.setInt(3, idRacTo);
                    s3.execute();

                    s3.setFloat(1, -sum);
                    s3.setFloat(2, -sum);
                    s3.setInt(3, idRacFrom);
                    s3.execute();

                    conn.commit();

                }

                System.out.println("Uspešna realizacija.");
                return true;
            }

        } catch (SQLException ex) {
            System.out.println("Dogodila se greška.");
            System.out.println(ex);

        }
        return false;

    }

}