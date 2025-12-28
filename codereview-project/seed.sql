-- pass: pass123, admin123
INSERT INTO users (username, email, password_hash, role)
VALUES
  ('admin', 'admin@example.com',
   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
   'ADMIN'),
  ('userA', 'userA@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER'),
  ('userB', 'userB@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER'),
  ('userC', 'userC@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER'),
  ('userD', 'userD@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER'),
  ('userE', 'userE@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER'),
  ('userF', 'userF@example.com',
   '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c',
   'USER')
ON CONFLICT (username) DO NOTHING;
