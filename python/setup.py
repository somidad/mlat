from setuptools import setup

install_requires = [
    'numpy',
    'pandas'
]

setup(
    name='mlat',
    version='0.1',
    description='Multilateration',
    author='Seokseong Jeon',
    author_email='sjeon87@gmail.com',
    packages=[
        'mlat'
    ],
    install_requires=install_requires
)

