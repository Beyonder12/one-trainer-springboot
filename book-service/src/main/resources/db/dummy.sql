INSERT INTO books (id, author, pages, publish_date, title) VALUES
(1, 'Haruki Murakami', 384, '2002-04-12', 'Kafka on the Shore'),
(2, 'George Orwell', 328, '1949-06-08', '1984'),
(3, 'J.K. Rowling', 410, '1997-06-26', 'Harry Potter and the Philosopher''s Stone'),
(4, 'J.R.R. Tolkien', 423, '1954-07-29', 'The Fellowship of the Ring'),
(5, 'F. Scott Fitzgerald', 180, '1925-04-10', 'The Great Gatsby'),
(6, 'Jane Austen', 279, '1813-01-28', 'Pride and Prejudice'),
(7, 'Dan Brown', 454, '2003-03-18', 'The Da Vinci Code'),
(8, 'Yuval Noah Harari', 498, '2011-09-04', 'Sapiens: A Brief History of Humankind'),
(9, 'Stephen King', 447, '1977-01-28', 'The Shining'),
(10, 'Mark Manson', 224, '2016-09-13', 'The Subtle Art of Not Giving a F*ck');


-- Same author

INSERT INTO books (id, author, pages, publish_date, title) VALUES
-- Haruki Murakami
(11, 'Haruki Murakami', 624, '1997-09-12', 'The Wind-Up Bird Chronicle'),
(12, 'Haruki Murakami', 320, '2004-10-17', 'After Dark'),

-- George Orwell
(13, 'George Orwell', 112, '1945-08-17', 'Animal Farm'),
(14, 'George Orwell', 296, '1933-03-12', 'Down and Out in Paris and London'),

-- J.K. Rowling
(15, 'J.K. Rowling', 341, '1998-07-02', 'Harry Potter and the Chamber of Secrets'),
(16, 'J.K. Rowling', 435, '1999-07-08', 'Harry Potter and the Prisoner of Azkaban'),

-- J.R.R. Tolkien
(17, 'J.R.R. Tolkien', 352, '1955-10-20', 'The Two Towers'),
(18, 'J.R.R. Tolkien', 416, '1955-10-20', 'The Return of the King'),

-- F. Scott Fitzgerald
(19, 'F. Scott Fitzgerald', 218, '1922-03-04', 'The Beautiful and Damned'),
(20, 'F. Scott Fitzgerald', 208, '1922-03-26', 'Tales of the Jazz Age'),

-- Jane Austen
(21, 'Jane Austen', 208, '1811-10-30', 'Sense and Sensibility'),
(22, 'Jane Austen', 432, '1814-05-04', 'Mansfield Park'),

-- Dan Brown
(23, 'Dan Brown', 410, '2000-05-01', 'Angels & Demons'),
(24, 'Dan Brown', 509, '2009-09-15', 'The Lost Symbol'),

-- Yuval Noah Harari
(25, 'Yuval Noah Harari', 464, '2015-09-08', 'Homo Deus: A Brief History of Tomorrow'),
(26, 'Yuval Noah Harari', 320, '2018-08-30', '21 Lessons for the 21st Century'),

-- Stephen King
(27, 'Stephen King', 704, '1986-09-15', 'IT'),
(28, 'Stephen King', 823, '2000-11-07', 'On Writing: A Memoir of the Craft'),

-- Mark Manson
(29, 'Mark Manson', 288, '2019-05-13', 'Everything is F*cked: A Book About Hope');
