# Build And Test

Le projet utilise Maven via le chemin local suivant :

`C:\Users\Pc\AppData\Local\Programs\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn.cmd`

Des scripts Windows sont fournis pour eviter de dependre du `PATH` global.

## Commandes

- Tests : [`scripts/test.cmd`](/C:/Users/Pc/Desktop/Projet/Architecture_client_serveur/heptathlon-client-server/scripts/test.cmd)
- Package : [`scripts/package.cmd`](/C:/Users/Pc/Desktop/Projet/Architecture_client_serveur/heptathlon-client-server/scripts/package.cmd)
- Site : [`scripts/site.cmd`](/C:/Users/Pc/Desktop/Projet/Architecture_client_serveur/heptathlon-client-server/scripts/site.cmd)
- Maven generique : [`scripts/mvn-local.cmd`](/C:/Users/Pc/Desktop/Projet/Architecture_client_serveur/heptathlon-client-server/scripts/mvn-local.cmd)

## Utilisation

Depuis la racine du projet :

```bat
scripts\test.cmd
scripts\package.cmd
scripts\site.cmd
```

Avec options Maven supplementaires :

```bat
scripts\test.cmd -e
scripts\mvn-local.cmd verify
scripts\mvn-local.cmd checkstyle:checkstyle
```

## Rapports utiles

- Resultats de tests : `target\surefire-reports`
- Couverture JaCoCo : `target\site\jacoco\index.html`
- Site Maven : `target\site\index.html`
