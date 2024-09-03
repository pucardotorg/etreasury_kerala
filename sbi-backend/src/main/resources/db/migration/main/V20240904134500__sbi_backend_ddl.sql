ALTER TABLE transaction_details
DROP COLUMN IF EXISTS success_url;

ALTER TABLE transaction_details
DROP COLUMN IF EXISTS fail_url;

