# Development Notes

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification. Commit messages are
checked using **commitlint** via a Husky `commit-msg` hook.

## 📝 Changelog

The changelog is generated automatically by a GitHub workflow whenever changes are pushed to the `main` branch. If you
need to create the changelog locally, run:

```bash
npm run changelog
```

## 🔧 Setup

To set up the Git hooks, install the Node dependencies once:

```bash
npm install
```

## 💻 Development Workflow

Run tests:
```bash
./gradlew clean test
```

Run the plugin in a development IDE instance:
```bash
./gradlew runIde
```

## 🔄 Continuous Integration

The project has several GitHub workflows:
- `Build & Test`: Runs on all pull requests to perform linting (detekt), build, and test the code

## 🚀 Publishing the Plugin

The `Sign & Publish` workflow no longer runs automatically on every push. To
publish a new version:

1. Push your changes to the `main` branch.
2. Open the **Actions** tab on GitHub.
3. Select **Sign & Publish** and choose the `main` branch.
4. Click **Run workflow**.
