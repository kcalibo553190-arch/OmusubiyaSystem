-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 21, 2026 at 03:26 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `shopeasecrm`
--

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `CustomerID` int(11) NOT NULL,
  `FullName` varchar(255) DEFAULT NULL,
  `Contact` varchar(100) DEFAULT NULL,
  `Address` text DEFAULT NULL,
  `Username` varchar(150) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `DateRegistered` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`CustomerID`, `FullName`, `Contact`, `Address`, `Username`, `PasswordHash`, `DateRegistered`) VALUES
(1, 'test account', '09069154552', 'Saypon Toril Davao City', 'Emtia', '049c5ff6b02f20b5ac574a7c54888ec22f1695d893e7495dbd4b484dc828108a', '2026-08-23 18:14:40'),
(2, 'test', 'test', 'test', 'test', '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', '2026-08-23 18:16:23'),
(3, 'what', 'what', 'what', 'what', '$2a$11$c4eWseSRJNw8WsAbZpJ/6OKy4XMJ4Xjn3XSVbjLh8UXtnPtreJ036', '2026-08-24 04:37:23'),
(4, 'jake kyle L baranggan', '09265430758', 'saypon sampaguita toril', 'kyle643', '$2a$11$guENafKZrCZIPuobDoa4pe3UiGC7AxOIzMERSsoIcAM4KbByeqdqC', '2026-08-24 05:23:28'),
(5, 'asus vivobook', 'blahblah@gmail.com', 'address', 'banana', '$2a$11$iFZj6KkbnzfD/no3TVszWOfstA16FhttgU9zxtwUog9fdm3i9hsg6', '2026-08-24 12:19:48'),
(6, 'heehee', 'heehee', 'heehee', 'heehee', '$2a$11$o25dO17ZCfmDm47XOnS.muHMTqsUnj2Ry7YSz/vRXO32CmEBuW0m.', '2026-09-02 12:18:28'),
(7, 'test', 'test', 'test', 'test111', '$2a$11$Z13g2wIzVNw4l3PvfpSheOTthhVmifk6gI8uEctgj8AY7QqlV4rEy', '2026-09-16 07:29:18');

-- --------------------------------------------------------

--
-- Table structure for table `inquiries`
--

CREATE TABLE `inquiries` (
  `InquiryID` int(11) NOT NULL,
  `CustomerUsername` varchar(150) DEFAULT NULL,
  `Subject` varchar(255) DEFAULT NULL,
  `Message` text DEFAULT NULL,
  `InquiryDate` datetime DEFAULT current_timestamp(),
  `Status` varchar(50) DEFAULT 'New'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inquiries`
--

INSERT INTO `inquiries` (`InquiryID`, `CustomerUsername`, `Subject`, `Message`, `InquiryDate`, `Status`) VALUES
(4, 'what', 'button 2', 'okay its all working now, great', '2026-08-24 13:46:05', 'New'),
(5, 'what', 'error', 'THE INQUIRY BUTTON DOES ITS JOB TWICE', '2026-08-24 13:46:21', 'New'),
(6, 'what', 'inquiry button2', 'test output', '2026-08-24 13:54:12', 'New'),
(7, 'test111', 'test output', 'test output', '2026-09-17 16:58:14', 'New'),
(8, 'test111', 'test message', 'test output 09 09 2026', '2026-09-19 10:07:00', 'New'),
(9, 'test111', 'labexam', '1st lab exam', '2026-09-19 10:18:07', 'New'),
(10, 'test111', 'output test', 'test message 101', '2026-09-19 10:56:08', 'New');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `OrderID` int(11) NOT NULL,
  `CustomerID` int(11) DEFAULT NULL,
  `OrderDate` datetime NOT NULL,
  `TotalAmount` decimal(10,2) NOT NULL,
  `OrderStatus` varchar(50) DEFAULT 'Pending',
  `PaymentStatus` varchar(50) DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`OrderID`, `CustomerID`, `OrderDate`, `TotalAmount`, `OrderStatus`, `PaymentStatus`) VALUES
(1, 2, '2026-08-23 19:11:19', 750.00, 'Placed', 'Pending'),
(2, 2, '2026-08-23 19:11:21', 900.00, 'Placed', 'Pending'),
(3, 2, '2026-08-23 19:11:23', 1967.00, 'Placed', 'Pending'),
(4, 2, '2026-08-23 19:42:33', 0.00, 'Placed', 'Pending'),
(5, 2, '2026-08-23 19:42:36', 0.00, 'Placed', 'Pending'),
(6, 2, '2026-08-23 19:42:42', 0.00, 'Placed', 'Pending'),
(7, 2, '2026-08-23 19:42:55', 0.00, 'Placed', 'Pending'),
(8, 2, '2026-08-23 19:43:01', 0.00, 'Placed', 'Pending'),
(9, 2, '2026-08-23 20:48:01', 0.00, 'Placed', 'Pending'),
(10, 2, '2026-08-23 20:48:04', 0.00, 'Placed', 'Pending'),
(11, 2, '2026-08-23 20:48:07', 0.00, 'Placed', 'Pending'),
(12, 2, '2026-08-23 20:48:08', 0.00, 'Placed', 'Pending'),
(13, 3, '2026-08-24 04:37:52', 0.00, 'Placed', 'Pending'),
(14, 3, '2026-08-24 04:37:56', 0.00, 'Placed', 'Pending'),
(15, 3, '2026-08-24 04:37:57', 0.00, 'Placed', 'Pending'),
(16, 3, '2026-08-24 04:46:42', 0.00, 'Placed', 'Pending'),
(17, 3, '2026-08-24 04:46:43', 0.00, 'Placed', 'Pending'),
(18, 3, '2026-08-24 04:50:33', 0.00, 'Placed', 'Pending'),
(19, 3, '2026-08-24 04:50:35', 0.00, 'Placed', 'Pending'),
(20, 3, '2026-08-24 04:50:36', 0.00, 'Placed', 'Pending'),
(21, 3, '2026-08-24 04:50:37', 0.00, 'Placed', 'Pending'),
(22, 3, '2026-08-24 04:50:39', 0.00, 'Placed', 'Pending'),
(23, 3, '2026-08-24 04:50:41', 0.00, 'Placed', 'Pending'),
(24, 3, '2026-08-24 04:50:42', 750.00, 'Placed', 'Pending'),
(25, 3, '2026-08-24 05:00:57', 0.00, 'Placed', 'Pending'),
(26, 3, '2026-08-24 05:00:59', 0.00, 'Placed', 'Pending'),
(27, 3, '2026-08-24 05:01:00', 0.00, 'Placed', 'Pending'),
(28, 3, '2026-08-24 05:01:01', 0.00, 'Placed', 'Pending'),
(29, 3, '2026-08-24 05:23:47', 0.00, 'Placed', 'Pending'),
(30, 3, '2026-08-24 05:23:52', 0.00, 'Placed', 'Pending'),
(31, 3, '2026-08-24 05:23:55', 0.00, 'Placed', 'Pending'),
(32, 3, '2026-08-24 05:23:57', 0.00, 'Placed', 'Pending'),
(33, 3, '2026-08-24 05:23:58', 0.00, 'Placed', 'Pending'),
(34, 3, '2026-08-24 05:43:11', 750.00, 'Placed', 'Pending'),
(35, 3, '2026-08-24 05:43:13', 900.00, 'Placed', 'Pending'),
(36, 3, '2026-08-24 05:43:15', 1967.00, 'Placed', 'Pending'),
(37, 3, '2026-08-24 05:43:16', 1967.00, 'Placed', 'Pending'),
(38, 3, '2026-08-24 05:45:44', 1967.00, 'Placed', 'Pending'),
(39, 3, '2026-08-24 05:45:47', 900.00, 'Placed', 'Pending'),
(40, 3, '2026-08-24 05:45:51', 2700.00, 'Placed', 'Pending'),
(41, 3, '2026-08-24 05:54:18', 2700.00, 'Placed', 'Pending'),
(42, 3, '2026-08-24 05:54:20', 750.00, 'Placed', 'Pending'),
(43, 6, '2026-09-02 12:21:01', 9000.00, 'Placed', 'Pending'),
(44, 6, '2026-09-02 12:21:07', 900.00, 'Placed', 'Pending'),
(48, 6, '2026-09-02 12:21:28', 10800.00, 'Placed', 'Pending'),
(49, 6, '2026-09-02 12:21:36', 21637.00, 'Placed', 'Pending'),
(50, 7, '2026-09-16 07:29:35', 900.00, 'Placed', 'Pending'),
(51, 7, '2026-09-16 07:30:47', 750.00, 'Placed', 'Pending'),
(52, 7, '2026-09-16 07:36:06', 20.00, 'Placed', 'Pending'),
(54, 7, '2026-09-17 08:57:59', 26250.00, 'Placed', 'Pending'),
(55, 7, '2026-09-17 08:59:47', 0.00, 'Placed', 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `OrderItemID` int(11) NOT NULL,
  `OrderID` int(11) NOT NULL,
  `ProductID` int(11) NOT NULL,
  `Quantity` int(11) NOT NULL DEFAULT 1,
  `UnitPrice` decimal(10,2) NOT NULL,
  `Subtotal` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`OrderItemID`, `OrderID`, `ProductID`, `Quantity`, `UnitPrice`, `Subtotal`) VALUES
(1, 34, 1, 1, 750.00, 750.00),
(2, 35, 2, 1, 900.00, 900.00),
(3, 36, 3, 1, 1967.00, 1967.00),
(4, 37, 3, 1, 1967.00, 1967.00),
(5, 38, 3, 1, 1967.00, 1967.00),
(6, 39, 2, 1, 900.00, 900.00),
(7, 40, 2, 3, 900.00, 2700.00),
(8, 41, 2, 3, 900.00, 2700.00),
(9, 42, 1, 1, 750.00, 750.00),
(10, 43, 1, 12, 750.00, 9000.00),
(11, 44, 2, 1, 900.00, 900.00),
(12, 48, 2, 12, 900.00, 10800.00),
(13, 49, 3, 11, 1967.00, 21637.00),
(14, 50, 2, 1, 900.00, 900.00),
(15, 51, 1, 1, 750.00, 750.00),
(16, 52, 2, 1, 20.00, 20.00),
(17, 54, 1, 35, 750.00, 26250.00),
(18, 55, 2, 1, 0.00, 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `ProductID` int(11) NOT NULL,
  `ProductName` varchar(255) NOT NULL,
  `Description` text DEFAULT NULL,
  `Price` decimal(10,2) NOT NULL DEFAULT 0.00,
  `StockQuantity` int(11) NOT NULL DEFAULT 0,
  `ImagePath` varchar(512) DEFAULT NULL,
  `CreatedAt` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`ProductID`, `ProductName`, `Description`, `Price`, `StockQuantity`, `ImagePath`, `CreatedAt`) VALUES
(1, 'Yoyo', 'An unresponsive Yo-yo, often used by professionals on stage during performances', 750.00, 0, NULL, '2026-08-24 13:39:38'),
(2, '3x3 Rubiks Cube', 'A high-end 3x3 Rubiks Cube, often used during tournaments', 900.00, 26, NULL, '2026-08-24 13:39:38'),
(3, 'Attack Shark X6', 'A mid ranged price gaming mouse, used by players that wants a step up gaming mouse without spending too much', 1967.00, 36, NULL, '2026-08-24 13:39:38'),
(4, 'tests product', 'test product', 100.00, 30, '', '2026-09-19 10:14:49'),
(5, 'itom', 'black lang sya', 50.00, 1, '', '2026-09-19 10:59:43');

-- --------------------------------------------------------

--
-- Table structure for table `promotions`
--

CREATE TABLE `promotions` (
  `PromotionID` int(11) NOT NULL,
  `ProductID` int(11) NOT NULL,
  `DiscountPercent` decimal(5,2) DEFAULT NULL,
  `PromoPrice` decimal(10,2) DEFAULT NULL,
  `Active` tinyint(1) NOT NULL DEFAULT 0,
  `StartDate` datetime DEFAULT NULL,
  `EndDate` datetime DEFAULT NULL,
  `CreatedAt` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotions`
--

INSERT INTO `promotions` (`PromotionID`, `ProductID`, `DiscountPercent`, `PromoPrice`, `Active`, `StartDate`, `EndDate`, `CreatedAt`) VALUES
(1, 3, 50.00, NULL, 1, '2026-09-16 15:29:57', '2026-09-20 15:29:57', '2026-09-16 15:30:30'),
(2, 2, 50.00, 20.00, 1, '2026-09-16 15:35:15', '2026-09-20 15:35:15', '2026-09-16 15:35:37'),
(3, 2, 100.00, NULL, 1, '2026-09-17 16:59:09', '2026-09-18 16:59:09', '2026-09-17 16:59:28');

-- --------------------------------------------------------

--
-- Table structure for table `staff`
--

CREATE TABLE `staff` (
  `StaffID` int(11) NOT NULL,
  `FirstName` varchar(200) DEFAULT NULL,
  `LastName` varchar(200) DEFAULT NULL,
  `Email` varchar(255) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `Role` varchar(50) DEFAULT 'Staff'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`CustomerID`),
  ADD UNIQUE KEY `Username` (`Username`);

--
-- Indexes for table `inquiries`
--
ALTER TABLE `inquiries`
  ADD PRIMARY KEY (`InquiryID`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`OrderID`),
  ADD KEY `CustomerID` (`CustomerID`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`OrderItemID`),
  ADD KEY `idx_order` (`OrderID`),
  ADD KEY `idx_product` (`ProductID`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`ProductID`);

--
-- Indexes for table `promotions`
--
ALTER TABLE `promotions`
  ADD PRIMARY KEY (`PromotionID`),
  ADD KEY `idx_promo_product` (`ProductID`);

--
-- Indexes for table `staff`
--
ALTER TABLE `staff`
  ADD PRIMARY KEY (`StaffID`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `CustomerID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `inquiries`
--
ALTER TABLE `inquiries`
  MODIFY `InquiryID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `OrderID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `OrderItemID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `ProductID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `promotions`
--
ALTER TABLE `promotions`
  MODIFY `PromotionID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `staff`
--
ALTER TABLE `staff`
  MODIFY `StaffID` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`CustomerID`) REFERENCES `customers` (`CustomerID`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_items_orders` FOREIGN KEY (`OrderID`) REFERENCES `orders` (`OrderID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_order_items_products` FOREIGN KEY (`ProductID`) REFERENCES `products` (`ProductID`) ON UPDATE CASCADE;

--
-- Constraints for table `promotions`
--
ALTER TABLE `promotions`
  ADD CONSTRAINT `fk_promotions_products` FOREIGN KEY (`ProductID`) REFERENCES `products` (`ProductID`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
