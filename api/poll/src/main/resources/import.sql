INSERT INTO comment(id, auteur, content) VALUES(nextval('hibernate_sequence'), 'komi','arthur leywin is grey!');
INSERT INTO mealpreference(id, userId, content) VALUES(nextval('hibernate_sequence'), 1,'spécialité malgache');
INSERT INTO poll(id, title, localisation, detail, clos, has_meal) VALUES(nextval('hibernate_sequence'), 'gl','antananarivo', 'preparer la présentation',false,false);