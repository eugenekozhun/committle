# Development Notes

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification. Commit messages are
checked using **commitlint** via a Husky `commit-msg` hook.

The changelog is generated automatically by a GitHub workflow whenever changes are pushed to the `main` branch. If you
need to create the changelog locally, run:

```bash
npm run changelog
```

To set up the Git hooks, install the Node dependencies once:

```bash
npm install
```

## Running the Gradle build and tests

```bash
./gradlew clean test
```

## Running the Gradle plugin

```bash
./gradlew runIde
```
