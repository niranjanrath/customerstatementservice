CREATE TABLE IF NOT EXISTS customer_statement_record (
  transaction_reference INT NOT NULL,
  account_number varchar(255) NOT NULL,
  start_balance int(100) NOT NULL,
  mutation int(100) NOT NULL,
  end_balance int(100) NOT NULL,
  description varchar(255),
  PRIMARY KEY (transaction_reference)
);