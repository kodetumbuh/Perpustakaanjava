-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.8.6-MariaDB - MariaDB Server
-- Server OS:                    Win64
-- HeidiSQL Version:             12.14.0.7165
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for perpustakaan_v2
CREATE DATABASE IF NOT EXISTS `perpustakaan_v2` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `perpustakaan_v2`;

-- Dumping structure for table perpustakaan_v2.anggota
CREATE TABLE IF NOT EXISTS `anggota` (
  `id_anggota` int(11) NOT NULL AUTO_INCREMENT,
  `no_anggota` varchar(20) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `jenis_kelamin` enum('L','P') DEFAULT NULL,
  `tempat_lahir` varchar(50) DEFAULT NULL,
  `tanggal_lahir` date DEFAULT NULL,
  `alamat` text DEFAULT NULL,
  `no_telepon` varchar(15) DEFAULT NULL,
  `no_barcode` bigint(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `no_identitas` varchar(25) DEFAULT NULL,
  `nama_photo` varchar(25) DEFAULT NULL,
  `tgl_daftar` date DEFAULT NULL,
  `tgl_expired` date DEFAULT NULL,
  `status` enum('aktif','nonaktif') DEFAULT 'aktif',
  PRIMARY KEY (`id_anggota`),
  UNIQUE KEY `no_anggota` (`no_anggota`),
  UNIQUE KEY `no_identitas` (`no_identitas`),
  KEY `idx_no_anggota` (`no_anggota`),
  KEY `idx_nama` (`nama`),
  KEY `idx_status` (`status`),
  KEY `idx_tgl_daftar` (`tgl_daftar`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.buku
CREATE TABLE IF NOT EXISTS `buku` (
  `id_buku` int(11) NOT NULL AUTO_INCREMENT,
  `isbn` varchar(20) DEFAULT NULL,
  `no_barcode` varchar(50) DEFAULT NULL,
  `judul` varchar(200) NOT NULL,
  `id_pengarang` int(11) NOT NULL,
  `id_penerbit` int(11) NOT NULL,
  `id_kategori` int(11) NOT NULL,
  `tahun_terbit` year(4) DEFAULT NULL,
  `edisi` varchar(50) DEFAULT NULL,
  `halaman` int(11) DEFAULT NULL,
  `bahasa` varchar(50) DEFAULT 'Indonesia',
  `id_rak` int(11) DEFAULT NULL,
  `stok_total` int(11) NOT NULL,
  `stok_tersedia` int(11) NOT NULL,
  `harga` decimal(10,2) DEFAULT NULL,
  `tgl_masuk` date DEFAULT NULL,
  `status` enum('tersedia','rusak','hilang') DEFAULT 'tersedia',
  PRIMARY KEY (`id_buku`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `id_rak` (`id_rak`),
  KEY `idx_isbn` (`isbn`),
  KEY `idx_judul` (`judul`),
  KEY `idx_kategori` (`id_kategori`),
  KEY `idx_pengarang` (`id_pengarang`),
  KEY `idx_penerbit` (`id_penerbit`),
  KEY `idx_status` (`status`),
  KEY `idx_stok` (`stok_tersedia`),
  CONSTRAINT `buku_ibfk_1` FOREIGN KEY (`id_pengarang`) REFERENCES `pengarang` (`id_pengarang`) ON UPDATE CASCADE,
  CONSTRAINT `buku_ibfk_2` FOREIGN KEY (`id_penerbit`) REFERENCES `penerbit` (`id_penerbit`) ON UPDATE CASCADE,
  CONSTRAINT `buku_ibfk_3` FOREIGN KEY (`id_kategori`) REFERENCES `kategori` (`id_kategori`) ON UPDATE CASCADE,
  CONSTRAINT `buku_ibfk_4` FOREIGN KEY (`id_rak`) REFERENCES `rak` (`id_rak`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.kategori
CREATE TABLE IF NOT EXISTS `kategori` (
  `id_kategori` int(11) NOT NULL AUTO_INCREMENT,
  `kode_kategori` varchar(10) NOT NULL,
  `nama_kategori` varchar(100) NOT NULL,
  `deskripsi` text DEFAULT NULL,
  PRIMARY KEY (`id_kategori`),
  UNIQUE KEY `kode_kategori` (`kode_kategori`),
  KEY `idx_kode` (`kode_kategori`),
  KEY `idx_nama` (`nama_kategori`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.peminjaman
CREATE TABLE IF NOT EXISTS `peminjaman` (
  `id_peminjaman` int(11) NOT NULL AUTO_INCREMENT,
  `no_peminjaman` varchar(30) NOT NULL,
  `id_anggota` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `tgl_peminjaman` date NOT NULL,
  `tgl_kembali_rencana` date NOT NULL,
  `tgl_kembali_aktual` date DEFAULT NULL,
  `denda` decimal(10,0) DEFAULT 0,
  `status` enum('dipinjam','dikembalikan') DEFAULT 'dipinjam',
  PRIMARY KEY (`id_peminjaman`),
  UNIQUE KEY `no_peminjaman` (`no_peminjaman`),
  KEY `id_user` (`id_user`),
  KEY `idx_no_peminjaman` (`no_peminjaman`),
  KEY `idx_anggota` (`id_anggota`),
  KEY `idx_status` (`status`),
  KEY `idx_tgl` (`tgl_peminjaman`),
  KEY `idx_tgl_kembali` (`tgl_kembali_rencana`),
  CONSTRAINT `peminjaman_ibfk_1` FOREIGN KEY (`id_anggota`) REFERENCES `anggota` (`id_anggota`) ON UPDATE CASCADE,
  CONSTRAINT `peminjaman_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.peminjaman_detail
CREATE TABLE IF NOT EXISTS `peminjaman_detail` (
  `id_detail` int(11) NOT NULL AUTO_INCREMENT,
  `id_peminjaman` int(11) NOT NULL,
  `id_buku` int(11) NOT NULL,
  `qty` int(11) NOT NULL DEFAULT 1,
  `catatan` text DEFAULT NULL,
  PRIMARY KEY (`id_detail`),
  UNIQUE KEY `unique_peminjaman_buku` (`id_peminjaman`,`id_buku`),
  KEY `idx_peminjaman` (`id_peminjaman`),
  KEY `idx_buku` (`id_buku`),
  CONSTRAINT `peminjaman_detail_ibfk_1` FOREIGN KEY (`id_peminjaman`) REFERENCES `peminjaman` (`id_peminjaman`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `peminjaman_detail_ibfk_2` FOREIGN KEY (`id_buku`) REFERENCES `buku` (`id_buku`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.penerbit
CREATE TABLE IF NOT EXISTS `penerbit` (
  `id_penerbit` int(11) NOT NULL AUTO_INCREMENT,
  `nama_penerbit` varchar(100) NOT NULL,
  `alamat` text DEFAULT NULL,
  `kota` varchar(50) DEFAULT NULL,
  `no_telepon` varchar(15) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_penerbit`),
  KEY `idx_nama` (`nama_penerbit`),
  KEY `idx_kota` (`kota`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.pengarang
CREATE TABLE IF NOT EXISTS `pengarang` (
  `id_pengarang` int(11) NOT NULL AUTO_INCREMENT,
  `nama_pengarang` varchar(150) NOT NULL,
  `negara` varchar(50) DEFAULT NULL,
  `biografi` text DEFAULT NULL,
  `tgl_lahir` date DEFAULT NULL,
  PRIMARY KEY (`id_pengarang`),
  KEY `idx_nama` (`nama_pengarang`),
  KEY `idx_negara` (`negara`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.pengembalian
CREATE TABLE IF NOT EXISTS `pengembalian` (
  `id_pengembalian` int(11) NOT NULL AUTO_INCREMENT,
  `id_peminjaman` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `tgl_pengembalian` date NOT NULL,
  `tgl_rencana` date DEFAULT NULL,
  `hari_terlambat` int(11) DEFAULT NULL,
  `tarif_denda_per_hari` decimal(10,2) DEFAULT 5000.00,
  `total_denda` decimal(10,2) DEFAULT 0.00,
  `kondisi_buku` enum('baik','rusak_ringan','rusak_berat','hilang') DEFAULT NULL,
  `status_pembayaran` enum('lunas','belum','cicil') DEFAULT 'belum',
  PRIMARY KEY (`id_pengembalian`),
  KEY `id_user` (`id_user`),
  KEY `idx_tgl` (`tgl_pengembalian`),
  KEY `idx_status` (`status_pembayaran`),
  KEY `idx_peminjaman` (`id_peminjaman`),
  CONSTRAINT `pengembalian_ibfk_1` FOREIGN KEY (`id_peminjaman`) REFERENCES `peminjaman` (`id_peminjaman`) ON UPDATE CASCADE,
  CONSTRAINT `pengembalian_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.properti
CREATE TABLE IF NOT EXISTS `properti` (
  `id_properti` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL DEFAULT 0,
  `nama_barang` varchar(50) NOT NULL,
  `status_barang` enum('baik','rusak ringan','rusak berat') NOT NULL DEFAULT 'baik',
  `qty_barang` int(10) NOT NULL DEFAULT 0,
  `keterangan` varchar(50) NOT NULL,
  `tgl_input` date DEFAULT NULL,
  PRIMARY KEY (`id_properti`),
  KEY `FK_properti_user` (`id_user`),
  CONSTRAINT `FK_properti_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.rak
CREATE TABLE IF NOT EXISTS `rak` (
  `id_rak` int(11) NOT NULL AUTO_INCREMENT,
  `kode_rak` varchar(10) NOT NULL,
  `lokasi` varchar(100) DEFAULT NULL,
  `kapasitas` int(11) DEFAULT NULL,
  `status` enum('tersedia','penuh') DEFAULT 'tersedia',
  PRIMARY KEY (`id_rak`),
  UNIQUE KEY `kode_rak` (`kode_rak`),
  KEY `idx_kode` (`kode_rak`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.reservasi
CREATE TABLE IF NOT EXISTS `reservasi` (
  `id_reservasi` int(11) NOT NULL AUTO_INCREMENT,
  `id_anggota` int(11) NOT NULL,
  `id_buku` int(11) NOT NULL,
  `tgl_reservasi` date NOT NULL,
  `tgl_berakhir` date NOT NULL,
  `status` enum('aktif','diambil','dibatalkan') DEFAULT 'aktif',
  PRIMARY KEY (`id_reservasi`),
  KEY `idx_anggota` (`id_anggota`),
  KEY `idx_buku` (`id_buku`),
  KEY `idx_status` (`status`),
  KEY `idx_tgl_reservasi` (`tgl_reservasi`),
  CONSTRAINT `reservasi_ibfk_1` FOREIGN KEY (`id_anggota`) REFERENCES `anggota` (`id_anggota`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `reservasi_ibfk_2` FOREIGN KEY (`id_buku`) REFERENCES `buku` (`id_buku`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for table perpustakaan_v2.user
CREATE TABLE IF NOT EXISTS `user` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) NOT NULL,
  `no_telepon` varchar(15) DEFAULT NULL,
  `role` enum('admin','petugas','kepala') DEFAULT 'petugas',
  `status` enum('aktif','nonaktif') DEFAULT 'aktif',
  `tgl_dibuat` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.

-- Dumping structure for trigger perpustakaan_v2.trg_peminjaman_detail_after_delete
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_peminjaman_detail_after_delete
AFTER DELETE ON peminjaman_detail
FOR EACH ROW
BEGIN
  UPDATE buku
  SET stok_tersedia = stok_tersedia + OLD.qty
  WHERE id_buku = OLD.id_buku;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger perpustakaan_v2.trg_peminjaman_detail_after_insert
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_peminjaman_detail_after_insert
AFTER INSERT ON peminjaman_detail
FOR EACH ROW
BEGIN
  UPDATE buku
  SET stok_tersedia = stok_tersedia - NEW.qty
  WHERE id_buku = NEW.id_buku
    AND stok_tersedia >= NEW.qty;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger perpustakaan_v2.trg_peminjaman_detail_after_update
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_peminjaman_detail_after_update
AFTER UPDATE ON peminjaman_detail
FOR EACH ROW
BEGIN
  
  IF NEW.id_buku = OLD.id_buku THEN
    UPDATE buku
    SET stok_tersedia = stok_tersedia + (OLD.qty - NEW.qty)
    WHERE id_buku = NEW.id_buku;
  ELSE
    
    UPDATE buku
    SET stok_tersedia = stok_tersedia + OLD.qty
    WHERE id_buku = OLD.id_buku;

    
    UPDATE buku
    SET stok_tersedia = stok_tersedia - NEW.qty
    WHERE id_buku = NEW.id_buku
      AND stok_tersedia >= NEW.qty;
  END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger perpustakaan_v2.trg_pengembalian_after_insert
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_pengembalian_after_insert
AFTER INSERT ON pengembalian
FOR EACH ROW
BEGIN
  
  UPDATE peminjaman
  SET status = 'dikembalikan',
      tgl_kembali_aktual = NEW.tgl_pengembalian,
      denda = NEW.total_denda
  WHERE id_peminjaman = NEW.id_peminjaman;
  
  
  UPDATE buku b
  INNER JOIN peminjaman_detail pd ON b.id_buku = pd.id_buku
  SET b.stok_tersedia = b.stok_tersedia + pd.qty
  WHERE pd.id_peminjaman = NEW.id_peminjaman;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger perpustakaan_v2.trg_pengembalian_before_insert
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_pengembalian_before_insert
BEFORE INSERT ON pengembalian
FOR EACH ROW
BEGIN
  
  IF NEW.tgl_rencana IS NOT NULL AND NEW.tgl_pengembalian IS NOT NULL THEN
    SET NEW.hari_terlambat = GREATEST(0, DATEDIFF(NEW.tgl_pengembalian, NEW.tgl_rencana));
    SET NEW.total_denda = NEW.hari_terlambat * NEW.tarif_denda_per_hari;
  END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

-- Dumping structure for trigger perpustakaan_v2.trg_pengembalian_before_update
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
DELIMITER //
CREATE TRIGGER trg_pengembalian_before_update
BEFORE UPDATE ON pengembalian
FOR EACH ROW
BEGIN
  
  IF NEW.tgl_rencana IS NOT NULL AND NEW.tgl_pengembalian IS NOT NULL THEN
    SET NEW.hari_terlambat = GREATEST(0, DATEDIFF(NEW.tgl_pengembalian, NEW.tgl_rencana));
    SET NEW.total_denda = NEW.hari_terlambat * NEW.tarif_denda_per_hari;
  END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
