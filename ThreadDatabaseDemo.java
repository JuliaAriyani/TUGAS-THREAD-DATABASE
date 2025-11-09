import java.sql.*; // Import library untuk koneksi dan operasi database (JDBC)
import java.text.SimpleDateFormat; // Untuk memformat tanggal dan waktu
import java.util.Date; // Untuk mendapatkan waktu saat ini

// ==========================
// 1️⃣ KELAS KONEKSI DATABASE
// ==========================
// Kelas ini bertanggung jawab untuk membuat koneksi ke database MySQL
class DatabaseConnection {
    // Konfigurasi koneksi MySQL: alamat server, nama database, user, dan password
    private static final String URL = "jdbc:mysql://localhost:3306/workshopdb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // username MySQL default
    private static final String PASSWORD = ""; // password MySQL (kosong jika belum diset)

    // Method untuk mendapatkan koneksi ke database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Muat driver JDBC MySQL agar Java tahu cara terhubung ke MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Coba hubungkan ke database menggunakan URL, user, dan password
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Jika driver JDBC tidak ditemukan (biasanya karena .jar belum ditambahkan)
            System.out.println("❌ Driver MySQL tidak ditemukan! Pastikan file mysql-connector-j-8.x.x.jar sudah disertakan.");
        } catch (SQLException e) {
            // Jika gagal koneksi ke database
            System.out.println("❌ Gagal terhubung ke database: " + e.getMessage());
        }
        return conn; // Kembalikan objek koneksi
    }
}

// ==========================
// 2️⃣ KELAS BOOKING SERVICE
// ==========================
// Kelas ini digunakan untuk menyimpan data pemesanan (booking) ke database
class BookingService {

    // Method synchronized agar hanya satu thread yang bisa menulis ke database pada waktu yang sama
    public synchronized void simpanBooking(String nama, String servis) {

        // Ambil koneksi ke database
        Connection conn = DatabaseConnection.getConnection();

        // Jika koneksi gagal, hentikan proses
        if (conn == null) {
            System.out.println("⚠️ Tidak dapat menyimpan booking karena koneksi gagal!");
            return;
        }

        // Query SQL untuk menambahkan data baru ke tabel booking
        String sql = "INSERT INTO booking (nama_pelanggan, jenis_servis, waktu_booking) VALUES (?, ?, ?)";

        // Gunakan PreparedStatement agar aman dari SQL Injection
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // Isi parameter query dengan data pelanggan
            ps.setString(1, nama);
            ps.setString(2, servis);
            ps.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); // waktu sekarang

            // Jalankan perintah SQL (INSERT)
            ps.executeUpdate();

            // Tampilkan pesan sukses
            System.out.println("✅ Booking disimpan: " + nama + " - " + servis);

        } catch (SQLException e) {
            // Jika ada error saat menyimpan data
            System.out.println("❌ Gagal menyimpan booking: " + e.getMessage());
        } finally {
            // Tutup koneksi agar tidak terjadi kebocoran memori
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("⚠️ Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}

// ==========================
// 3️⃣ KELAS THREAD PELANGGAN
// ==========================
// Kelas ini mewakili satu pelanggan yang sedang melakukan pemesanan
// Setiap pelanggan berjalan pada thread terpisah
class PelangganThread extends Thread {
    private String nama; // nama pelanggan
    private String servis; // jenis servis yang dipesan
    private BookingService service; // objek untuk menyimpan data ke database

    // Konstruktor untuk menginisialisasi data pelanggan
    public PelangganThread(String nama, String servis, BookingService service) {
        this.nama = nama;
        this.servis = servis;
        this.service = service;
    }

    // Method yang dijalankan ketika thread dimulai
    public void run() {
        System.out.println("🧍 " + nama + " sedang memesan layanan...");
        // Simpan data pemesanan ke database
        service.simpanBooking(nama, servis);
    }
}

// ==========================
// 4️⃣ KELAS UTAMA (MAIN CLASS)
// ==========================
// Kelas utama tempat program dieksekusi
public class ThreadDatabaseDemo {
    public static void main(String[] args) {
        // Membuat satu objek BookingService yang dipakai bersama oleh semua thread
        BookingService service = new BookingService();

        // Membuat tiga pelanggan (thread berbeda)
        Thread p1 = new PelangganThread("Andi", "Ganti Oli", service);
        Thread p2 = new PelangganThread("Budi", "Tune Up", service);
        Thread p3 = new PelangganThread("Citra", "Servis Rem", service);

        // Jalankan ketiga pelanggan secara bersamaan (multithreading)
        p1.start();
        p2.start();
        p3.start();
    }
}
