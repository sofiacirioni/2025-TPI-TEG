export class Dummy {
  id: number;
  name: string;

  constructor(id: number, name: string) {
    this.id = id;
    this.name = name;
  }

  sayHello(): string {
    return `Hola, mi nombre es ${this.name} y mi ID es ${this.id}.`;
  }
}
