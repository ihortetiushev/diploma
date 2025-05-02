ALTER TABLE income_category ADD COLUMN keywords TEXT;
ALTER TABLE expenses_category ADD COLUMN keywords TEXT;

-- Update keywords for income category
UPDATE income_category
SET keywords = 'зарплата,заробіток,переказ'
WHERE name = 'Заробітня плата';

-- Update keywords for expense categories
UPDATE expenses_category
SET keywords = 'вода,плов,novus,м\'ясо,glovo,атб,roshen,сільпо,хліб'
WHERE name = 'Продукти';

UPDATE expenses_category
SET keywords = 'одяг,взуття,шкарпетки,sinsay,h&m'
WHERE name = 'Одяг';

UPDATE expenses_category
SET keywords = 'аптека,ліки,анальгін,вітаміни,парацетамол'
WHERE name = 'Медицина';

UPDATE expenses_category
SET keywords = 'netflix,кіно,концерт'
WHERE name = 'Розваги';

UPDATE expenses_category
SET keywords = 'zoomagazin,корм,ветеринар,повідець'
WHERE name = 'Тварини';

UPDATE expenses_category
SET keywords = 'aliexpress,аврора,torgovelnyi center,eva,швабра,порошок'
WHERE name = 'Господарчі товари';

UPDATE expenses_category
SET keywords = 'заправка,бензин,wog,okko,ремонт,шиномантаж'
WHERE name = 'Авто';
