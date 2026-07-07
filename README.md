# CustomEnchantments

Плагин для Paper/Spigot 1.21.8 с кастомными чарами, книгами и поддержкой внешних jar-модулей чаров.

## Команды

- `/emchantment <name>` - наложить чар на предмет в руке.
- `/cenchant <name>` - алиас для `/emchantment`.
- `/cenchantment <name> [player]` - выдать книгу кастомного чара.

У кастомных чаров нет уровней: каждый чар либо есть на предмете, либо его нет.

## Права

- `customenchants.command.emchantment`
- `customenchants.command.cenchantment`
- `customenchants.admin`

## Встроенные чары

- `magnetism` - сразу складывает в инвентарь только дроп блоков, которые игрок сам сломал.
  Если инвентарь заполнен, лишние предметы падают на землю с небольшой задержкой подбора.
- `autosmelt` - переплавляет дроп и выдает опыт за переплавку.

Книги можно применять двумя способами:

- перетащить книгу на предмет в инвентаре;
- положить предмет и книгу в наковальню.

## Внешние чары

Папка создается автоматически:

`plugins/CustomEnchantments/enchantments/`

В нее можно положить jar-файл чара, например:

`plugins/CustomEnchantments/enchantments/my_enchant.jar`

Jar должен содержать реализацию `ru.customenchants.api.CustomEnchantmentProvider` и файл:

`META-INF/services/ru.customenchants.api.CustomEnchantmentProvider`

В этом файле указывается полный класс провайдера.

## Сборка

```bash
mvn package
```

Готовый плагин будет в `target/CustomEnchantments.jar`.
