package jerukperaspragita;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane; 

public class cKoneksi {

    private static Connection koneksi; // Gunakan static agar koneksi bisa dipakai bersama

    public static Connection getKoneksi() {
        if (koneksi == null) { // Hanya buat koneksi jika belum ada
            try {
                // 1. Daftarkan Driver (cara modern, tidak selalu eksplisit dibutuhkan di JDBC 4.0+)
                Class.forName("com.mysql.cj.jdbc.Driver"); //

                // 2. Siapkan detail koneksi
                // Pastikan nama database benar sesuai skrip SQL Anda
                String url = "jdbc:mysql://localhost:3306/dbjerukperas?useSSL=false&serverTimezone=Asia/Jakarta";
                String user = "root"; // User default XAMPP/Laragon
                String password = ""; // Password default XAMPP/Laragon (kosong)

                // 3. Buat Koneksi
                koneksi = DriverManager.getConnection(url, user, password);
                System.out.println("Koneksi ke Database Berhasil.");
                return koneksi;

            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Driver MySQL tidak ditemukan! Pastikan file .jar sudah ditambahkan ke Libraries proyek Anda.");
                System.err.println("Error ClassNotFound: " + e.getMessage());
                System.exit(0); // Keluar dari aplikasi jika driver tidak ada
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Koneksi ke Database Gagal! Pastikan XAMPP/MySQL sudah berjalan dan database 'dbjerukperas' ada.\nError: " + e.getMessage());
                System.err.println("Error SQL: " + e.getMessage());
                System.exit(0); // Keluar dari aplikasi jika koneksi gagal
            }
        }
        return koneksi; // Mengembalikan koneksi yang sudah ada
    }

    public static void closeKoneksi() {
        if (koneksi != null) {
            try {
                koneksi.close();
                koneksi = null;
                System.out.println("Koneksi database ditutup.");
            } catch (SQLException e) {
                System.err.println("Gagal menutup koneksi database: " + e.getMessage());
            }
        }
    }
}